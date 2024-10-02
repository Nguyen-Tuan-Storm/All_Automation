import argparse
import psutil
import os


def shutdown_servers_and_get_terminals(ports):
    terminals_to_close = []
    for port in ports:
        for conn in psutil.net_connections(kind='inet'):
            if conn.laddr.port == port:
                try:
                    process_pid = conn.pid
                    process = psutil.Process(process_pid)
                    process.terminate()
                    print(f"Shutting down server running on port {port}")

                    # Find the parent process tree to identify Terminal.app PID
                    parent = process.parent()
                    while parent:
                        if 'Terminal' in parent.name():
                            terminals_to_close.append(parent.pid)
                            break
                        parent = parent.parent()

                except psutil.NoSuchProcess:
                    pass
                except psutil.AccessDenied:
                    print(f"Access denied to process on port {port}")
    return terminals_to_close


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Shutdown servers based on a list of ports.')
    parser.add_argument('ports', type=str, help='Comma-separated list of ports to shut down')
    args = parser.parse_args()

    ports = [int(port) for port in args.ports.split(',')]
    terminals_to_close = shutdown_servers_and_get_terminals(ports)

    # Get the directory of the current script
    script_dir = os.path.dirname(os.path.abspath(__file__))
    temp_file_path = os.path.join(script_dir, 'terminals_to_close.txt')

    # Write the terminal PIDs to a file
    with open(temp_file_path, 'w') as f:
        for pid in terminals_to_close:
            f.write(f"{pid}\n")
