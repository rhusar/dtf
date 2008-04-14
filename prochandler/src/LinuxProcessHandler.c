/*
  JBoss, Home of Professional Open Source
  Copyright 2008, Red Hat Middleware LLC, and individual contributors
  as indicated by the @author tags.
  See the copyright.txt in the distribution for a
  full listing of individual contributors.
  This copyrighted material is made available to anyone wishing to use,
  modify, copy, or redistribute it subject to the terms and conditions
  of the GNU Lesser General Public License, v. 2.1.
  This program is distributed in the hope that it will be useful, but WITHOUT A
  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
  You should have received a copy of the GNU Lesser General Public License,
  v.2.1 along with this distribution; if not, write to the Free Software
  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  MA  02110-1301, USA.

  (C) 2008,
  @author JBoss Inc.
*/


#include <stdlib.h>
#include <signal.h>
#include <stdio.h>
#include <string.h>

// Process id of child process
pid_t pid = -1;

void termSignalHandler(int sig)
{
    printf("Terminate signal received: ");

    if ( pid != -1 )
    {
        printf("Terminating child process (%i)\n", pid);
        if ( kill (pid, SIGKILL) == 0 )
        {
            printf("Terminate was successful\n");
        }
        else
        {
            printf("Terminate was not successful\n");
        }
    }
    else
    {
        printf("Child process not started ignoring\n");
    }

    // Clean term signal
    signal(sig, termSignalHandler);
}

int main(int argc, char** argv)
{
    signal(SIGTERM, termSignalHandler);

    printf("Linux Specific Process Handler v0.1alpha (%i)\n", argc);

    if ( argc <= 1 )
    {
        printf("No parameters passed - expected at least one!\n");
        exit(0);
    }

    pid = fork();

    if ( pid == 0 ) // If we are the child process
    {
        char* params[argc];
        int count;

        for (count=0;count<argc-1;count++)
        {
            params[count] = malloc(strlen(argv[count+1])+2);
            strcpy(params[count], argv[count+1]);
            printf("params[%i]: %s\n", count, params[count]);
        }

        params[count] = NULL;

        printf("Executing %s (%i)\n", argv[1], argc-1);

        int returnValue = execv(argv[1], params);

        if ( returnValue == -1 )
        {
            printf("Failed to execute the child process!\n");
        }

        printf("Cleaning up\n");
        for (count=0;count<argc-1;count++)
        {
            free(params[count]);
        }
    }
    else
    {
        int status;

        // We are the parent process
        // We need to wait for the child to complete
        printf("Child started with %i pid\n", pid);
        waitpid(pid, &status, 0);
        printf("Child completed.\n");
    }
}
