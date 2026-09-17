"""Run an already-provisioned isolated Forge server with timed console commands.
Requires a marker file; never points at a real pack or world. Logs remain inside the test server.
"""
import argparse
import json
from pathlib import Path
import subprocess
import threading
import time

def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("--server", required=True, type=Path)
    p.add_argument("--java", required=True)
    p.add_argument("--commands", type=Path)
    p.add_argument("--timeout", type=int, default=240)
    args = p.parse_args()
    server = args.server.resolve()
    if not (server / "KNCraft-ISOLATED-TEST-WORLD").is_file(): raise SystemExit("Refusing unmarked server")
    commands = json.loads(args.commands.read_text()) if args.commands else [[1, "kncraft status"], [2, "reload"], [20, "stop"]]
    proc = subprocess.Popen([args.java, "-Xms1G", "-Xmx5G", "-Dkncraft.isolatedTests=true", "@libraries/net/minecraftforge/forge/1.20.1-47.4.0/win_args.txt", "nogui"],
        cwd=server, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True, encoding="utf-8", errors="replace",
        creationflags=getattr(subprocess, "CREATE_NO_WINDOW", 0))
    ready = threading.Event()
    def read():
        with (server / "smoke-console.log").open("w", encoding="utf-8") as log:
            for line in proc.stdout:
                log.write(line); log.flush()
                if 'Done (' in line and 'For help' in line: ready.set()
    thread = threading.Thread(target=read, daemon=True); thread.start()
    start = time.monotonic()
    while not ready.wait(.5) and proc.poll() is None and time.monotonic()-start < args.timeout: pass
    if ready.is_set():
        print("Dedicated server ready", flush=True)
        for delay, command in commands:
            time.sleep(delay)
            if proc.poll() is not None: break
            proc.stdin.write(command + "\n"); proc.stdin.flush()
            print("Sent:", command, flush=True)
    if proc.poll() is None:
        if not ready.is_set(): print("Startup timeout; requesting stop", flush=True)
        try: proc.stdin.write("stop\n"); proc.stdin.flush(); proc.wait(timeout=60)
        except (subprocess.TimeoutExpired, BrokenPipeError): proc.terminate(); proc.wait(timeout=15)
    thread.join(timeout=5)
    print("Exit:", proc.returncode, "Ready:", ready.is_set(), "Log:", server / "smoke-console.log")
    if not ready.is_set() or proc.returncode != 0: raise SystemExit(1)

if __name__ == "__main__": main()
