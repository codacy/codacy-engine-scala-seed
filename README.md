[![Codacy Badge](https://api.codacy.com/project/badge/grade/bc3a79d1b12649158a1eb4758e872141)](https://www.codacy.com/app/Codacy/codacy-engine-scala-seed)
[![Codacy Badge](https://api.codacy.com/project/badge/coverage/bc3a79d1b12649158a1eb4758e872141)](https://www.codacy.com/app/Codacy/codacy-engine-scala-seed)
[![Build Status](https://circleci.com/gh/codacy/codacy-engine-scala-seed.svg?style=shield&circle-token=:circle-token)](https://circleci.com/gh/codacy/codacy-engine-scala-seed)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.codacy/codacy-engine-scala-seed_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.codacy/codacy-engine-scala-seed_2.11)

# Engine Scala Seed

We use external tools at Codacy, this is the library we use across the multiple external tools integrations.
For more details and examples of tools that use this project, you can check
[PMDJava](https://github.com/codacy/codacy-pmdjava),
[ESLint](https://github.com/codacy/codacy-eslint) and
[Pylint](https://github.com/codacy/codacy-pylint).

### Usage

Add to your SBT dependencies:

```
"com.codacy" %% "codacy-engine-scala-seed" % "1.4.0"
```

You shouldn't worry about the library itself, we use it as a core in our tools,
and everything is well explained in our Docs section.

## Docs

[Docker Docs](http://docs.codacy.com/v1.5/docs/tool-developer-guide)

[Scala Docker Template Docs](http://docs.codacy.com/v1.5/docs/tool-developer-guide-using-scala)

## Test

Follow the instructions at [codacy-plugins-test](https://github.com/codacy/codacy-plugins-test/blob/master/README.md#test-definition)