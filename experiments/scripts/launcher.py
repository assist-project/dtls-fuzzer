import sys
import os
import os.path
import subprocess
import argparse

# simple script for launching experiments

JAR_LOCATION="dtls-fuzzer.jar"
LIB_DIR="lib"
COMMAND_TEMPLATE="java -jar {} @{} "

def build_command(arg_file, jar_file, other_args):
    cmd = "java -jar " + jar_file
    if arg_file is not None:
        cmd+= " @" + arg_file
    cmd += " " +" ".join(other_args)
    return cmd

def launch_experiment(arg_file, jar_file, redir, other_args = []):
    command = build_command(arg_file, jar_file, other_args)
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
    return (command, subp)

def launch_experiments(args_location, jar_file, redir, other_args = []):
    if args_location is not None and os.path.isdir(args_location):
        args_files = [os.path.join(args_location,args_file) for args_file in os.listdir(args_location)]
    else:
        args_files = [args_location]
    experiments = []
    for args_file in args_files:
        experiments.append(launch_experiment(args_file, jar_file, redir, other_args=other_args))
    return experiments

def log_experiments(experiments, log_file):
    with open(log_file, "w") as f:
        for (cmd, proc) in experiments:
            f.write("command: \"{}\"; pid: {}\n".format(cmd, proc.pid))

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Utility for launching multiple instances of DTLS-Fuzzer')
    parser.add_argument('-a', "--args", required=False, type=str, help="directory with argument files or argument file")
    parser.add_argument('-r', "--redirect", action='store_true', help="redirect process output/error to stdout/stderr")
    parser.add_argument('-l', "--log", required=False, type=str, default=None, help="optional log file to which command/process information is written")
    parser.add_argument('-j', "--jar", required=False, type=str, default=JAR_LOCATION, help="DTLS-Fuzzer .jar file")
    parser.add_argument('-p', "--params", required=False, nargs='+', default=[], help="parameters to pass to the learning processes (which augment/overwrite those in the argument files)")
    args = parser.parse_args()
    experiments = launch_experiments(args.args, args.jar, args.redirect, other_args=args.params)
    if args.log is not None:
        log_experiments(experiments, args.log)
