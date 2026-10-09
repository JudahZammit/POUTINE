import subprocess
import sys
from pathlib import Path


def main():
    jar = Path(sys.prefix, "share", "poutine", "poutine.jar")
    sys.exit(subprocess.call(["java", "-jar", str(jar), *sys.argv[1:]]))
