```
git tag v1.0.0-SNAPSHOT
sbt 'set ThisBuild / version := "1.0.0-SNAPSHOT"' 'publish'
git push origin HEAD --tags
```