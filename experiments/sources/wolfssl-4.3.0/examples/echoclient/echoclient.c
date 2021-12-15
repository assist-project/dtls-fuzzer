/* echoclient.c
 *
 * Copyright (C) 2006-2020 wolfSSL Inc.
 *
 * This file is part of wolfSSL.
 *
 * wolfSSL is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * wolfSSL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1335, USA
 */


#ifdef HAVE_CONFIG_H
    #include <config.h>
#endif

#include <cyassl/ctaocrypt/settings.h>
/* let's use cyassl layer AND cyassl openssl layer */
#include <cyassl/ssl.h>
#include <cyassl/openssl/ssl.h>
#ifdef CYASSL_DTLS
    #include <cyassl/error-ssl.h>
#endif

#if defined(WOLFSSL_MDK_ARM) || defined(WOLFSSL_KEIL_TCP_NET)
        #include <stdio.h>
        #include <string.h>
        #include "cmsis_os.h"
        #include "rl_fs.h"
        #include "rl_net.h"
        #include "wolfssl_MDK_ARM.h"
#endif

#include <cyassl/test.h>

#include <examples/echoclient/echoclient.h>

#ifndef NO_WOLFSSL_CLIENT

#ifdef WOLFSSL_ASYNC_CRYPT
    static int devId = INVALID_DEVID;
#endif


void echoclient_test(void* args)
{
    SOCKET_T sockfd = 0;

    FILE* fin   = stdin  ;
    FILE* fout = stdout;

#ifndef WOLFSSL_MDK_SHELL
    int inCreated  = 0;
    int outCreated = 0;
#endif

    char msg[1024];
    char reply[1024+1];

    SSL_METHOD* method = 0;
    SSL_CTX*    ctx    = 0;
    SSL*        ssl    = 0;

    int ret = 0, err = 0;
    int doDTLS = 0;
    int sendSz;
#ifndef WOLFSSL_MDK_SHELL
    int argc    = 0;
    char** argv = 0;
#endif
    word16 port = yasslPort;
    char buffer[CYASSL_MAX_ERROR_SZ];

    ((func_args*)args)->return_code = -1; /* error state */



#ifndef WOLFSSL_MDK_SHELL
    argc = ((func_args*)args)->argc;
    argv = ((func_args*)args)->argv;

    if (argc >= 2) {
        fin  = fopen(argv[1], "r");
        inCreated = 1;
    }
    if (argc >= 3) {
        fout = fopen(argv[2], "w");
        outCreated = 1;
    }
#endif

    if (!fin)  err_sys("can't open input file");
    if (!fout) err_sys("can't open output file");

#ifdef CYASSL_DTLS
    doDTLS  = 1;
#endif

#if defined(NO_MAIN_DRIVER) && !defined(USE_WINDOWS_API) && !defined(WOLFSSL_MDK_SHELL)
    port = ((func_args*)args)->signal->port;
#endif

#if defined(CYASSL_DTLS)
    method  = DTLSv1_2_client_method();
#elif !defined(NO_TLS)
    method = CyaSSLv23_client_method();
#elif defined(WOLFSSL_ALLOW_SSLV3)
    method = SSLv3_client_method();
#else
    #error "no valid client method type"
#endif
    ctx    = SSL_CTX_new(method);

#ifndef NO_FILESYSTEM
    #ifndef NO_RSA
    if (SSL_CTX_load_verify_locations(ctx, caCertFile, 0) != WOLFSSL_SUCCESS)
        err_sys("can't load ca file, Please run from wolfSSL home dir");
    #endif
    #ifdef HAVE_ECC
        if (SSL_CTX_load_verify_locations(ctx, caEccCertFile, 0) != WOLFSSL_SUCCESS)
            err_sys("can't load ca file, Please run from wolfSSL home dir");
    #elif defined(HAVE_ED25519)
        if (SSL_CTX_load_verify_locations(ctx, caEdCertFile, 0) != WOLFSSL_SUCCESS)
            err_sys("can't load ca file, Please run from wolfSSL home dir");
    #elif defined(HAVE_ED448)
        if (SSL_CTX_load_verify_locations(ctx, caEd448CertFile, 0) != WOLFSSL_SUCCESS)
            err_sys("can't load ca file, Please run from wolfSSL home dir");
    #endif
#elif !defined(NO_CERTS)
        load_buffer(ctx, caCertFile, WOLFSSL_CA);
#endif

    CyaSSL_CTX_set_psk_client_callback(ctx, my_psk_client_cb);

    char ciphers[WOLFSSL_CIPHER_LIST_MAX_SIZE];

    ret = wolfSSL_get_ciphers(ciphers, (int)sizeof(ciphers));

    if (CyaSSL_CTX_set_cipher_list(ctx,ciphers) !=WOLFSSL_SUCCESS)
        err_sys("client can't set cipher list 2");


#ifdef WOLFSSL_ENCRYPTED_KEYS
    SSL_CTX_set_default_passwd_cb(ctx, PasswordCallBack);
#endif

#if defined(WOLFSSL_MDK_ARM)
    CyaSSL_CTX_set_verify(ctx, WOLFSSL_VERIFY_NONE, 0);
#endif

#ifdef WOLFSSL_ASYNC_CRYPT
    ret = wolfAsync_DevOpen(&devId);
    if (ret < 0) {
        printf("Async device open failed\nRunning without async\n");
    }
    wolfSSL_CTX_UseAsync(ctx, devId);
#endif /* WOLFSSL_ASYNC_CRYPT */

    ssl = SSL_new(ctx);
    tcp_connect(&sockfd, yasslIP, port, doDTLS, 0, ssl);

    SSL_set_fd(ssl, sockfd);
#if defined(USE_WINDOWS_API) && defined(CYASSL_DTLS) && defined(NO_MAIN_DRIVER)
    /* let echoserver bind first, TODO: add Windows signal like pthreads does */
    Sleep(100);
#endif

    do {
        err = 0; /* Reset error */
        ret = SSL_connect(ssl);
        if (ret != WOLFSSL_SUCCESS) {
            err = SSL_get_error(ssl, 0);
        #ifdef WOLFSSL_ASYNC_CRYPT
            if (err == WC_PENDING_E) {
                ret = wolfSSL_AsyncPoll(ssl, WOLF_POLL_FLAG_CHECK_HW);
                if (ret < 0) break;
            }
        #endif
        }
    } while (err == WC_PENDING_E);
    if (ret != WOLFSSL_SUCCESS) {
        printf("SSL_connect error %d, %s\n", err,
            ERR_error_string(err, buffer));
        err_sys("SSL_connect failed");
    }

#ifdef CYASSL_DTLS
    while (1) {
        do {
            err = 0; /* reset error */
            ret = SSL_read(ssl, reply, sizeof(reply)-1);
            if (ret <= 0) {
                err = SSL_get_error(ssl, 0);
            #ifdef WOLFSSL_ASYNC_CRYPT
                if (err == WC_PENDING_E) {
                    ret = wolfSSL_AsyncPoll(ssl, WOLF_POLL_FLAG_CHECK_HW);
                    if (ret < 0) break;
                }
            #endif
            }
        } while (err == WC_PENDING_E);

        if (ret > 0) {
            reply[ret] = 0;
            fputs(reply, fout);
            fflush(fout) ;
            sendSz = ret;
        } else if (wolfSSL_dtls(ssl) && err == DECRYPT_ERROR) {
                    /* This condition is OK. The packet should be dropped
                    * silently when there is a decrypt or MAC error on
                    * a DTLS record. */
                    continue;
        } else {
            printf("SSL_read msg error %d, %s\n", err,
                ERR_error_string(err, buffer));
            err_sys("SSL_read failed");
            break;
        }

        strncpy(msg, reply, sendSz);
        sendSz = (int)strlen(msg);
        /* try to tell server done */
        do {
            err = 0; /* reset error */
            ret = SSL_write(ssl, msg, sendSz);
            if (ret <= 0) {
                err = SSL_get_error(ssl, 0);
            #ifdef WOLFSSL_ASYNC_CRYPT
                if (err == WC_PENDING_E) {
                    ret = wolfSSL_AsyncPoll(ssl, WOLF_POLL_FLAG_CHECK_HW);
                    if (ret < 0) break;
                }
            #endif
            }
        } while (err == WC_PENDING_E);

        if (ret != sendSz) {
            printf("SSL_write msg error %d, %s\n", err,
                ERR_error_string(err, buffer));
            err_sys("SSL_write failed");
            break;
        }
    }
#else
    SSL_shutdown(ssl);
#endif

    SSL_free(ssl);
    SSL_CTX_free(ctx);

#ifdef WOLFSSL_ASYNC_CRYPT
    wolfAsync_DevClose(&devId);
#endif

    fflush(fout);
#ifndef WOLFSSL_MDK_SHELL
    if (inCreated)  fclose(fin);
    if (outCreated) fclose(fout);
#endif

    CloseSocket(sockfd);
    ((func_args*)args)->return_code = 0;
}

#endif /* !NO_WOLFSSL_CLIENT */

/* so overall tests can pull in test function */
#ifndef NO_MAIN_DRIVER

    int main(int argc, char** argv)
    {
        func_args args;

#ifdef HAVE_WNR
        if (wc_InitNetRandom(wnrConfig, NULL, 5000) != 0)
            err_sys("Whitewood netRandom global config failed");
#endif

        StartTCP();

        args.argc = argc;
        args.argv = argv;
        args.return_code = 0;

        CyaSSL_Init();
#if defined(DEBUG_CYASSL) && !defined(WOLFSSL_MDK_SHELL)
        CyaSSL_Debugging_ON();
#endif
#ifndef CYASSL_TIRTOS
        ChangeToWolfRoot();
#endif
#ifndef NO_WOLFSSL_CLIENT
        echoclient_test(&args);
#endif

        CyaSSL_Cleanup();

#ifdef HAVE_WNR
        if (wc_FreeNetRandom() < 0)
            err_sys("Failed to free netRandom context");
#endif /* HAVE_WNR */

        return args.return_code;
    }

#endif /* NO_MAIN_DRIVER */
