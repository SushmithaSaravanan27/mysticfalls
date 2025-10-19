#!/usr/bin/env python3
"""
AmazeCare Hospital Management System - Application Launcher
This script helps start and manage both the backend and frontend services.
"""

import os
import sys
import subprocess
import signal
import time
from pathlib import Path

# Color codes for terminal output
class Colors:
    HEADER = '\033[95m'
    BLUE = '\033[94m'
    CYAN = '\033[96m'
    GREEN = '\033[92m'
    WARNING = '\033[93m'
    FAIL = '\033[91m'
    ENDC = '\033[0m'
    BOLD = '\033[1m'

# Project paths
BACKEND_DIR = Path("amazecare-backend/amazecare-backend")
FRONTEND_DIR = Path("amazecare-hospitalmanagement/amazecare-hospitalmanagement")

# Process tracking
processes = []

def print_header(message):
    """Print a formatted header message"""
    print(f"\n{Colors.HEADER}{Colors.BOLD}{'='*60}{Colors.ENDC}")
    print(f"{Colors.HEADER}{Colors.BOLD}{message.center(60)}{Colors.ENDC}")
    print(f"{Colors.HEADER}{Colors.BOLD}{'='*60}{Colors.ENDC}\n")

def print_info(message):
    """Print an info message"""
    print(f"{Colors.CYAN}ℹ {message}{Colors.ENDC}")

def print_success(message):
    """Print a success message"""
    print(f"{Colors.GREEN}✓ {message}{Colors.ENDC}")

def print_error(message):
    """Print an error message"""
    print(f"{Colors.FAIL}✗ {message}{Colors.ENDC}")

def print_warning(message):
    """Print a warning message"""
    print(f"{Colors.WARNING}⚠ {message}{Colors.ENDC}")

def signal_handler(sig, frame):
    """Handle Ctrl+C gracefully"""
    print_warning("\n\nShutting down services...")
    for process in processes:
        try:
            process.terminate()
            process.wait(timeout=5)
        except:
            process.kill()
    print_success("All services stopped.")
    sys.exit(0)

def check_java():
    """Check if Java is installed"""
    try:
        result = subprocess.run(["java", "-version"], 
                              capture_output=True, 
                              text=True)
        return result.returncode == 0
    except FileNotFoundError:
        return False

def check_maven():
    """Check if Maven is installed"""
    try:
        result = subprocess.run(["mvn", "-version"], 
                              capture_output=True, 
                              text=True)
        return result.returncode == 0
    except FileNotFoundError:
        return False

def check_node():
    """Check if Node.js is installed"""
    try:
        result = subprocess.run(["node", "--version"], 
                              capture_output=True, 
                              text=True)
        return result.returncode == 0
    except FileNotFoundError:
        return False

def check_npm():
    """Check if npm is installed"""
    try:
        result = subprocess.run(["npm", "--version"], 
                              capture_output=True, 
                              text=True)
        return result.returncode == 0
    except FileNotFoundError:
        return False

def install_frontend_dependencies():
    """Install frontend dependencies"""
    print_info("Installing frontend dependencies...")
    try:
        subprocess.run(["npm", "install"], 
                      cwd=FRONTEND_DIR, 
                      check=True)
        print_success("Frontend dependencies installed successfully")
        return True
    except subprocess.CalledProcessError:
        print_error("Failed to install frontend dependencies")
        return False

def start_backend():
    """Start the Spring Boot backend"""
    print_header("Starting Backend Service")
    
    if not BACKEND_DIR.exists():
        print_error(f"Backend directory not found: {BACKEND_DIR}")
        return None
    
    print_info("Starting Spring Boot application...")
    print_info("Backend will be available at: http://localhost:8080")
    
    try:
        # Use Maven wrapper to run the application
        if (BACKEND_DIR / "mvnw").exists():
            cmd = ["./mvnw", "spring-boot:run"]
        else:
            cmd = ["mvn", "spring-boot:run"]
        
        process = subprocess.Popen(
            cmd,
            cwd=BACKEND_DIR,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            text=True,
            bufsize=1
        )
        
        print_success("Backend service starting...")
        return process
    
    except Exception as e:
        print_error(f"Failed to start backend: {e}")
        return None

def start_frontend():
    """Start the Angular frontend"""
    print_header("Starting Frontend Service")
    
    if not FRONTEND_DIR.exists():
        print_error(f"Frontend directory not found: {FRONTEND_DIR}")
        return None
    
    # Check if node_modules exists
    if not (FRONTEND_DIR / "node_modules").exists():
        print_warning("node_modules not found. Installing dependencies...")
        if not install_frontend_dependencies():
            return None
    
    print_info("Starting Angular development server...")
    print_info("Frontend will be available at: http://localhost:4200")
    
    try:
        process = subprocess.Popen(
            ["npm", "start"],
            cwd=FRONTEND_DIR,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            text=True,
            bufsize=1
        )
        
        print_success("Frontend service starting...")
        return process
    
    except Exception as e:
        print_error(f"Failed to start frontend: {e}")
        return None

def main():
    """Main function to orchestrate the application startup"""
    signal.signal(signal.SIGINT, signal_handler)
    
    print_header("AmazeCare Hospital Management System")
    print_info("Checking system requirements...\n")
    
    # Check prerequisites
    all_ok = True
    
    if check_java():
        print_success("Java is installed")
    else:
        print_error("Java is not installed or not in PATH")
        all_ok = False
    
    if check_maven():
        print_success("Maven is installed")
    else:
        print_warning("Maven is not installed or not in PATH (will use Maven wrapper if available)")
    
    if check_node():
        print_success("Node.js is installed")
    else:
        print_error("Node.js is not installed or not in PATH")
        all_ok = False
    
    if check_npm():
        print_success("npm is installed")
    else:
        print_error("npm is not installed or not in PATH")
        all_ok = False
    
    if not all_ok:
        print_error("\nPlease install missing requirements before running this script.")
        sys.exit(1)
    
    print("\n" + "="*60 + "\n")
    print("Select an option:")
    print("  1. Start Backend only")
    print("  2. Start Frontend only")
    print("  3. Start Both (Backend + Frontend)")
    print("  4. Install Frontend Dependencies")
    print("  5. Exit")
    print("\n" + "="*60 + "\n")
    
    choice = input("Enter your choice (1-5): ").strip()
    
    if choice == "1":
        backend = start_backend()
        if backend:
            processes.append(backend)
            print_info("\nPress Ctrl+C to stop the backend service")
            backend.wait()
    
    elif choice == "2":
        frontend = start_frontend()
        if frontend:
            processes.append(frontend)
            print_info("\nPress Ctrl+C to stop the frontend service")
            frontend.wait()
    
    elif choice == "3":
        backend = start_backend()
        if backend:
            processes.append(backend)
            time.sleep(5)  # Give backend time to start
        
        frontend = start_frontend()
        if frontend:
            processes.append(frontend)
        
        if processes:
            print_header("Services Running")
            print_success("Backend: http://localhost:8080")
            print_success("Frontend: http://localhost:4200")
            print_info("\nPress Ctrl+C to stop all services")
            
            try:
                for process in processes:
                    process.wait()
            except KeyboardInterrupt:
                signal_handler(None, None)
    
    elif choice == "4":
        install_frontend_dependencies()
    
    elif choice == "5":
        print_info("Exiting...")
        sys.exit(0)
    
    else:
        print_error("Invalid choice. Please run the script again.")
        sys.exit(1)

if __name__ == "__main__":
    main()
