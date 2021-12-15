import sys
import os
import os.path
import subprocess
import argparse
import time

# simple script for launching experiments

JAR_LOCATION="target/dtls-fuzzer.jar"
LIB_DIR="lib"
COMMAND_TEMPLATE="java -jar {} @{} "
is_dry=False

def build_command(tool, arg_file, jar_file, other_args):
    (jvm_params, app_params)=other_args
    cmd = "java -jar " + " ".join(jvm_params) + jar_file
    if tool is not None:
        cmd+= " " + tool
    if arg_file is not None:
        cmd+= " @" + arg_file
    cmd += " " +" ".join(app_params)
    return cmd

def launch_experiment(tool, arg_file, jar_file, redir, other_args):
    command = build_command(tool, arg_file, jar_file, other_args)
    subp = None
    if not is_dry:
        if redir:
            redir_out, redir_err=sys.stdout, sys.stderr
        else:
            redir_out, redir_err=subprocess.DEVNULL, subprocess.DEVNULL
        subp = subprocess.Popen(command, shell=True, 
            stdout=redir_out, 
            stderr=redir_err, 
            close_fds=True,
            cwd=os.getcwd())
        print("Launched command: \"", command, "\" with process pid: ", subp.pid)
        time.sleep(2)
    else:
        print("Would launch command \"", command, "\"")
    return (command, subp)



def log_experiments(experiments, log_file):
    with open(log_file, "w") as f:
        for (cmd, proc) in experiments:
            if proc is not None:
                f.write("command: \"{}\"; pid: {}\n".format(cmd, proc.id))
            else:
                f.write("command: \"{}\"".format(cmd))

def get_argfiles(arg_locations):
    arg_files=[]
    for arg_location in arg_locations:
        if arg_location is not None and os.path.isdir(arg_location):
            location_arg_files = [os.path.join(arg_location, arg_file) for arg_file in os.listdir(arg_location)]
        else:
            location_arg_files = [arg_location]
        arg_files.extend(location_arg_files)
    return arg_files


def launch_experiments(pcount, tool, arg_locations, jar_file, redir, other_args, log=None):
    arg_files = get_argfiles(arg_locations)

    arg_file_groups = [arg_files[k:k+pcount] for k in range(0, len(arg_files), pcount)]
    group_num = len(arg_file_groups)
    group_idx = 0

    for arg_file_group in arg_file_groups:
        experiments = []
        for arg_file in arg_file_group:  
            experiments.append(launch_experiment(tool, arg_file, jar_file, redir, other_args))
        if log is not None:    
            log_experiments(experiments, log)
        if group_idx < group_num-1:
            print("Waiting for all experiments of this group to finish before launching the next group of experiments")
            if not is_dry:
                for _,subp in experiments:
                    print("Waiting for ", subp.pid)
                    subp.wait()
        else:
            print("Launched the last group of experiments, thus exiting.")
        group_idx += 1

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Utility for launching multiple dtls-fuzzer experiments.')

    parser.add_argument('-t', "--tool", required=False, default=None, type=str, help="which tool to run (state-fuzzer-client, state-fuzzer-server...). The tool may already be specified in the arguments file, hence this command is only optional")
    parser.add_argument('-r', "--redirect", action='store_true', help="redirect process output/error to stdout/stderr")
    parser.add_argument('-d', "--dryrun", action='store_true', help="only logs the commands it would run without executing them")
    parser.add_argument('-l', "--log", required=False, type=str, default=None, help="optional log file to which command/process information is written")
    parser.add_argument('-j', "--jar", required=False, type=str, default=JAR_LOCATION, help="dtls-fuzzer .jar file")
    parser.add_argument('-pc', "--processcount", required=False, type=int, default=20, help="the maximum number of sub-processes that can be executed simultaneuosly. Setting this to 1 means sequential execution")
   
    parser.add_argument('-jp', "--jvmparams", required=False, nargs='+', default=[], help="parameters to pass to the java virtual machine")
    parser.add_argument('-ap', "--appparams", required=False, nargs='+', default=[], help="parameters to pass to the learning application (which augment/overwrite those in the argument files)")

    parser.add_argument("args", type=str, help="a sequence of directories with argument files or individual argument files. dtls-fuzzer will be executed for each argument file via a subprocess", nargs='+')
    args = parser.parse_args()
    if len(sys.argv) == 1:
        parser.print_usage()
        exit(0)
    is_dry = args.dryrun
    app_params = list(args.appparams)
    jvm_params = list(args.jvmparams)
    launch_experiments(args.processcount, args.tool, args.args, args.jar, args.redirect, (jvm_params, app_params), args.log)