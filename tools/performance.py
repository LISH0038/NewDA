#!/usr/bin/env python3

import os
import time
import threading, subprocess
from enum import Enum
import signal
import argparse
import random

PROCESSES_BASE_IP = 11000

logsDir = "../example/output/"

class ProcessState(Enum):
    RUNNING = 1
    TERMINATED = 2

class ProcessInfo:
    def __init__(self, handle):
        self.lock = threading.Lock()
        self.handle = handle
        self.state = ProcessState.RUNNING

class Validation:
    def __init__(self, procs, msgs):
        self.processes = procs
        self.messages = msgs

    def generateLcausalConfig(self, directory):
        hostsfile = os.path.join(directory, 'hosts')
        configfile = os.path.join(directory, 'config')


        with open(hostsfile, 'w') as hosts:
            for i in range(1, self.processes + 1):
                hosts.write("{} localhost {}\n".format(i, PROCESSES_BASE_IP+i))

        with open(configfile, 'w') as config:
            config.write("{}\n".format(self.messages))
            for i in range(1, self.processes + 1):
                others = list(range(1, self.processes + 1))
                others.remove(i)
                random.shuffle(others)
                if len(others) // 2 > 0:
                    others = others[0: len(others) // 2]

                config.write("{} {}\n".format(i, ' '.join(map(str, others))))

        return (hostsfile, configfile)

class PerformanceTest:
    def __init__(self, procs):
        self.processes = procs
        self.processesInfo = dict()
        for (logicalPID, handle) in procs:
            self.processesInfo[logicalPID] = ProcessInfo(handle)

    def terminatedAllProcesses(self):
        for _, info in self.processesInfo.items():
            with info.lock:
                if info.state != ProcessState.TERMINATED:
                   info.handle.send_signal(signal.SIGTERM)
        return False

def startProcesses(processes, runscript, hostsFilePath, configFilePath, outputDir):
    runscriptPath = os.path.abspath(runscript)
    if not os.path.isfile(runscriptPath):
        raise Exception("`{}` is not a file".format(runscriptPath))

    if os.path.basename(runscriptPath) != 'run.sh':
        raise Exception("`{}` is not a runscript".format(runscriptPath))

    outputDirPath = os.path.abspath(outputDir)
    if not os.path.isdir(outputDirPath):
        raise Exception("`{}` is not a directory".format(outputDirPath))

    baseDir, _ = os.path.split(runscriptPath)
    bin_cpp = os.path.join(baseDir, "bin", "da_proc")
    bin_java = os.path.join(baseDir, "bin", "da_proc.jar")

    if os.path.exists(bin_cpp):
        cmd = [bin_cpp]
    elif os.path.exists(bin_java):
        cmd = ['java', '-jar', bin_java]
    else:
        raise Exception("`{}` could not find a binary to execute. Make sure you build before validating".format(runscriptPath))

    procs = []
    for pid in range(1, processes+1):
        cmd_ext = ['--id', str(pid),
                   '--hosts', hostsFilePath,
                   '--output', os.path.join(outputDirPath, 'proc{:02d}.output'.format(pid)),
                   configFilePath]

        stdoutFd = open(os.path.join(outputDirPath, 'proc{:02d}.stdout'.format(pid)), "w")
        stderrFd = open(os.path.join(outputDirPath, 'proc{:02d}.stderr'.format(pid)), "w")


        procs.append((pid, subprocess.Popen(cmd + cmd_ext, stdout=stdoutFd, stderr=stderrFd)))

    return procs

def main(processes, messages, runscript, duration):
    validation = Validation(processes, messages)
    hostsFile, configFile = validation.generateLcausalConfig(logsDir)

    try:
        procs = startProcesses(processes, runscript, hostsFile, configFile, logsDir)

        pt = PerformanceTest(procs)

        for (logicalPID, procHandle) in procs:
            print("Process with logicalPID {} has PID {}".format(logicalPID, procHandle.pid))

        time.sleep(duration)

        pt.terminatedAllProcesses()

        mutex = threading.Lock()

        def waitForProcess(logicalPID, procHandle, mutex):
            procHandle.wait()

            with mutex:
                print("Process {} exited with {}".format(logicalPID, procHandle.returncode))

        # Monitor which processes have exited
        monitors = [threading.Thread(target=waitForProcess, args=(logicalPID, procHandle, mutex)) for (logicalPID, procHandle) in procs]
        [p.start() for p in monitors]
        [p.join() for p in monitors]

    finally:
            if procs is not None:
                for _, p in procs:
                    p.kill()

def measure(processes):
    from statistics import mean

    outputs = ["{}proc0{}.output".format(logsDir, i) for i in range(1, processes+1)]
    broadcasts = []
    delivers = []

    for output in outputs:
        with open(output, "r") as f:
            broadcast = 0
            deliver = 0

            for no, line in enumerate(f):
                tokens = line.split()

                if tokens[0] == 'b':
                    broadcast += 1

                if tokens[0] == 'd':
                    deliver += 1

            broadcasts.append(broadcast)
            delivers.append(deliver)

    print("Processes={}, Average Broadcast={}, Average Deliver={}".format(processes, mean(broadcasts), mean(delivers)))

if __name__ == "__main__":
    parser = argparse.ArgumentParser()

    parser.add_argument(
        "-r",
        "--runscript",
        required=True,
        dest="runscript",
        help="Path to run.sh",
    )

    parser.add_argument(
        "-p",
        "--processes",
        required=True,
        type=int,
        dest="processes",
        help="Number of processes that broadcast",
    )

    parser.add_argument(
        "-m",
        "--messages",
        required=True,
        type=int,
        dest="messages",
        help="Maximum number (because it can crash) of messages that each process can broadcast",
    )

    parser.add_argument(
        "-d",
        "--duration",
        required=True,
        type=int,
        dest="duration",
        help="Duration for which a test runs",
    )

    results = parser.parse_args()

    main(results.processes, results.messages, results.runscript, results.duration)
    measure(results.processes)

