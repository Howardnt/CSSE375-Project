                                     __  __                              __            __                                     
                                    /  |/  |                            /  |          /  |                                    
        ______    _______   _______ $$/ $$/         ______    ______   _$$ |_         $$ |____    ______    ______    ______  
       /      \  /       | /       |/  |/  |       /      \  /      \ / $$   |        $$      \  /      \  /      \  /      \ 
       $$$$$$  |/$$$$$$$/ /$$$$$$$/ $$ |$$ |       $$$$$$  |/$$$$$$  |$$$$$$/         $$$$$$$  |/$$$$$$  |/$$$$$$  |/$$$$$$  |
       /    $$ |$$      \ $$ |      $$ |$$ |       /    $$ |$$ |  $$/   $$ | __       $$ |  $$ |$$    $$ |$$ |  $$/ $$    $$ |
      /$$$$$$$ | $$$$$$  |$$ \_____ $$ |$$ |      /$$$$$$$ |$$ |        $$ |/  |      $$ |  $$ |$$$$$$$$/ $$ |      $$$$$$$$/ 
      $$    $$ |/     $$/ $$       |$$ |$$ |      $$    $$ |$$ |        $$  $$/       $$ |  $$ |$$       |$$ |      $$       |
       $$$$$$$/ $$$$$$$/   $$$$$$$/ $$/ $$/        $$$$$$$/ $$/          $$$$/        $$/   $$/  $$$$$$$/ $$/        $$$$$$$/ 

**Wiki Page - Planned Architecture**

**Define Design Purpose**

**List Usability Attributes and decribe how we enable them**

**How our primary functionality works and how it will interact with users**

Our system takes a Java project and scans it for design problems using a set of linter checks, where each check looks for a specific issue like a design principle violation or a bad pattern. Users run the tool and choose which project and checks they want to use, and the system then executes those checks on the code. As the checks run, the system collects any issues it finds. Once finished, it generates a simple report. This report explains what design problems were found in a clear manner.

**Which reference architecture are we using**
