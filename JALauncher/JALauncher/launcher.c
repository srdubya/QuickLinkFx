//
//  launcher.c
//  JALauncher
//
//  Created by Steve Wiley on 3/22/20.
//  Copyright © 2020 Steve Wiley. All rights reserved.
//

#include "launcher.h"
#include <jni.h>

typedef int (JNICALL *JLI_Launch_t)(int argc,
                                    char ** argv,
                                    int jargc,
                                    const char** jargv,
                                    int appclassc,
                                    const char** appclassv,
                                    const char* fullversion,
                                    const char* dotversion,
                                    const char* pname,
                                    const char* lname,
                                    jboolean javaargs,
                                    jboolean cpwildcard,
                                    jboolean javaw,
                                    jint ergo
                                    );

int launch(int argc, char ** argv, char * libfname) {
    void *libJLI = dlopen(libfname, RTLD_LAZY);
    
    JLI_Launch_t jli_launchFxnPtr = NULL;
    if (libJLI != NULL) {
        jli_launchFxnPtr = dlsym(libJLI, "JLI_Launch");
    }
    
    if (jli_launchFxnPtr == NULL) {
        return -1;
    }
    
    return jli_launchFxnPtr(argc,
                            argv,
                            0, NULL,
                            0, NULL,
                            "",
                            "",
                            "java",
                            "java",
                            JNI_FALSE,
                            JNI_FALSE,
                            JNI_FALSE,
                            0);
}
