# Mobile Computing Labs

This repository holds the course labs, one folder per lab. Each lab is a self-contained project, so open the folder for the lab you are working on. New lab folders are added as the course progresses, so a later branch also contains the earlier labs.

| Lab | Folder | Topic | Open with |
|---|---|---|---|
| 1 | [`lab1-kotlin-clinic`](lab1-kotlin-clinic) | Kotlin clinic | IntelliJ IDEA or a terminal (pure JVM) |
| 2 | [`lab2-compose`](lab2-compose) | Compose: fundamentals to navigation | Android Studio |
| 3 | [`lab3-ai-prototype`](lab3-ai-prototype) | AI-assisted prototype (create your own app) | A guide plus templates, not a project |
| 4 | [`posts-app`](posts-app) | Architecture (ViewModel, UDF) | Android Studio |

Labs 4 to 10 all live in the one `posts-app` folder, which grows a layer each lab.

Each lab folder has its own `README.md` with setup and step-by-step instructions. Start there.

Lab 3 is the pivot. It has no starter code to fill in: you invent your own app and build a prototype in a new project you create. The `lab3-ai-prototype` folder holds the guide and the templates you copy into that project.

The branch you are on is a step in the course. A `labN-starter` branch contains the exercises to do (marked `TODO()`); the matching `labN-solution` branch adds the reference solution, so `git diff labN-starter labN-solution` shows exactly that solution. Try the exercises first; when you compare, read why the solution works rather than copying.

From Lab 3 onward you create and grow your own app in its own repository, so it is one evolving project rather than a new folder per lab.
