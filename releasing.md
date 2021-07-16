```
git tag v1.0.0-SNAPSHOT
sbt 'set ThisBuild / version := "1.0.0-SNAPSHOT"' 'publish' '++ 3.0.1' 'publish'
git push origin HEAD --tags
```