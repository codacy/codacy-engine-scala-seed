# Engine Scala Seed

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/bc3a79d1b12649158a1eb4758e872141)](https://www.codacy.com/gh/codacy/codacy-engine-scala-seed?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=codacy/codacy-engine-scala-seed&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/bc3a79d1b12649158a1eb4758e872141)](https://www.codacy.com/gh/codacy/codacy-engine-scala-seed?utm_source=github.com&utm_medium=referral&utm_content=codacy/codacy-engine-scala-seed&utm_campaign=Badge_Coverage)
[![Build Status](https://circleci.com/gh/codacy/codacy-engine-scala-seed.svg?style=shield&circle-token=:circle-token)](https://circleci.com/gh/codacy/codacy-engine-scala-seed)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.codacy/codacy-engine-scala-seed_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.codacy/codacy-engine-scala-seed_2.12)

Framework to help integration with external analysis tools at Codacy. 
These tools provide the issues you can see on Codacy after an analysis is completed.

For more details and examples of tools that use this project, you can check:
* [PMD](https://github.com/codacy/codacy-pmd)
* [ESLint](https://github.com/codacy/codacy-eslint)
* [Pylint](https://github.com/codacy/codacy-pylint)

## Usage

Add to your SBT dependencies:

```scala
"com.codacy" %% "codacy-engine-scala-seed" % "<VERSION>"
```

## Docs

### How to integrate an external analysis tool on Codacy

#### Requirements

* Docker definition with the tool you want to integrate
* Define the documentation for the patterns provided by the tool

#### Assumptions and Behaviour

* To run the tool we provide the configuration file, `/.codacyrc`, with the language to run and optional parameters a tool might need.
* The source code to be analysed will be located in `/src`, meaning that when provided in the configuration, the file paths are relative to `/src`.

* **Structure of the .codacyrc file:**
  * **files:** Files to be analysed (their path is relative to `/src`)
  * **tools:** Array of tools
    * **name:** Unique identifier of the tool. This will be provided by the tool in patterns.json file.
    * **patterns:** Array of patterns that must be checked
      * **patternId:** Unique identifier of the pattern
      * **parameters:** Parameters of the pattern
        * **name:** Unique identifier of the parameter
        * **value:** Value to be used as parameter value

```json
{
  "files" : ["foo/bar/baz.js", "foo2/bar/baz.php"],
  "tools":[
    {
      "name":"jshint",
      "patterns":[
        {
          "patternId":"latedef",
          "parameters":[
            {
              "name":"latedef",
              "value":"vars"
            }
          ]
        }
      ]
    }
  ]
}
```

Regarding the configuration file, the tool should have different behaviours for the following situations:
* If `/.codacyrc` exists and has files and patterns, use them to run.
* If `/.codacyrc` exists and only has patterns and no files, use the patterns to invoke the tool for all files from /src
(files should be searched recursively for all folders in /src).
* If `/.codacyrc` exists and has only files and no patterns, run only for those files and look 
for the tool's native configuration file, if the tool supports it.
* If `/.codacyrc` does not exist or any of its contents (files or patterns) is not available, 
you should invoke the tool for all files from /src (files should be searched recursively for all folders in /src) 
and check them with the tool's native configuration file, if it is supported and if it exists. Otherwise, run the tool with the default patterns.
* If `/.codacyrc` fails to be parsed, throw an error.

**Exit codes**
* The exit codes can be different, depending if the tool invocation is successful or not:
  * :tada: **0**: The tool executed successfully
  * :cold_sweat: **1**: An unknown error occurred while running the tool 
  * :alarm_clock: **2**: Execution timeout

**Notes:**

* To run the tool in debug mode, so you can have more detailed logs, you need to set the environment variable `DEBUG` to `true` when invoking the docker.
* To configure a different timeout for the tool, you have to set the environment variable `TIMEOUT` when invoking the docker, setting it with values like `10 seconds`, `30 minutes` or `2 hours`.

## Setup
1. Write the docker file that will run the tool.
   * It must have a binary entry point without any parameters.
     * Notice that if you decide to use this seed, you can use the [**sbt-native-packager**](https://github.com/sbt/sbt-native-packager) plugin
     and run `sbt docker:publishLocal` that generates the dockerfile automatically and publishes the docker locally.

2. Write a patterns.json with the configuration of your tool.
    * This file must be located on /docs/patterns.json.
      * **name:** Unique identifier of the tool (lower-case letters without spaces)
      * **version:** Tool version to display in the Codacy UI
      * **patterns:** The patterns that the tool provides
          * **patternId:** Unique identifier of the pattern (lower-case letters without spaces)
          * **level:** Severity level of the issue 
          * **category:** Category of the issue 
          * **parameters:** Parameters received by the pattern
            * **name:** Unique identifier of the parameter (lower-case letters without spaces)
            * **default:** Default value of the parameter
    ```json
    {
      "name":"jshint",
      "version": "1.2.3",
      "patterns":[
        {
          "patternId": "latedef",
          "category": "ErrorProne",
          "parameters": [
            {
              "name": "latedef",
              "default": "nofunc"
            }
          ],
          "level": "Warning"
        }
      ]
    }
    ```
    #### Levels and Categories
    For level types we have:
    * Error
    * Warning
    * Info
    
    For category types we have:
    * ErrorProne
    * CodeStyle
    * Complexity
    * UnusedCode
    * Security
    * Compatibility
    * Performance
    * Documentation
    * BestPractice

3. Write the code to run the tool
You don't have to use this seed and you can write the code in any language you want but, you have to invoke the tool according to the configuration.
After you have your results from the tool, you should print them to the standard output in our Result format, one result per line.
    * The filename should **not** include the prefix "/src/". Example: 
        * absolute path: /src/folder/file.js
        * filename path: folder/file.js
    ```json
    {
      "filename":"codacy/core/test.js",
      "message":"found this in your code",
      "patternId":"latedef",
      "line":2
    }
    ```
    * If you are not able to run the analysis for any of the files requested you should return an error for each one of them to the standard output in our Error format.
    ```json
    {
      "filename":"codacy/core/test.js",
      "message":"could not parse the file"
    }
    ```

## Tool Documentation
At Codacy we strive to provide the best value to our users and, to accomplish that, we document our patterns so that the user can better understand the problem and fix it.

At this point, your tool has everything it needs to run, but there is one other really important thing that you should do before submitting your docker: the documentation for your tool.

Your files for this section should be placed in /docs/description/.

In order to provide more details you can create:
* A single /docs/description/description.json file.
* A /docs/description/<PATTERN-ID>.md file for each pattern. 
This documentation should also be generated automatically to avoid having to go through all of the files each time it needs to be updated.

In the description.json you define the title for the pattern, brief description, time to fix (in minutes), and also a description of the parameters in the following format:    
```json
[
  {
    "patternId": "latedef",
    "title": "Enforce variable def before use",
    "description": "Prohibits the use of a variable before it was defined.",
    "parameters": [
      {
        "name": "latedef",
        "description": "Declaration order verification. Check all [true] | Do not check functions [nofunc]"
      }
    ],
    "timeToFix": 10
  }
]
```

To give a more detailed explanation about the issue, you should define the <PATTERN-ID>.md. Example:

```markdown
Fields in interfaces are automatically public static final, and methods are public abstract.
Classes or interfaces nested in an interface are automatically public and static (all nested interfaces are automatically static).

For historical reasons, modifiers which are implied by the context are accepted by the compiler, but are superfluous.

Ex:

    public interface Foo {
        public abstract void bar();         // both abstract and public are ignored by the compiler
        public static final int X = 0;         // public, static, and final all ignored
        public static class Bar {}             // public, static ignored
        public static interface Baz {}         // ditto
        
        void foo();                            //this is correct
    }

    public class Bar {
        public static interface Baz {} // static ignored
    }

[Source](http://pmd.sourceforge.net/pmd-5.3.2/pmd-java/rules/java/unusedcode.html#UnusedModifier)
```

You should explain the what and why of the issue. Adding an example is always a nice way to help other people understand the problem. For a more thorough explanation you can also add a link at the end referring a more complete source.

## Test

Follow the instructions at [codacy-plugins-test](https://github.com/codacy/codacy-plugins-test/blob/master/README.md#test-definition).

## Dockerizing

**Running the docker**
```bash
docker run -t \
--net=none \
--privileged=false \
--cap-drop=ALL \
--user=docker \
--rm=true \
-v <PATH-TO-FOLDER-WITH-FILES-TO-CHECK>:/src:ro \
<YOUR-DOCKER-NAME>:<YOUR-DOCKER-VERSION>
```

**Docker restrictions**
* Docker image size should not exceed 500MB
* Docker should contain a non-root user named docker with UID/GID 2004

## What is Codacy

[Codacy](https://www.codacy.com/) is an Automated Code Review Tool that monitors your technical debt, helps you improve your code quality, teaches best practices to your developers, and helps you save time in Code Reviews.

### Among Codacy’s features

* Identify new Static Analysis issues
* Commit and Pull Request Analysis with GitHub, BitBucket/Stash, GitLab (and also direct git repositories)
* Auto-comments on Commits and Pull Requests
* Integrations with Slack, HipChat, Jira, YouTrack
* Track issues in Code Style, Security, Error Proneness, Performance, Unused Code and other categories

Codacy also helps keep track of Code Coverage, Code Duplication, and Code Complexity.

Codacy supports PHP, Python, Ruby, Java, JavaScript, and Scala, among others.

### Free for Open Source

Codacy is free for Open Source projects.
