```
git tag v0.0.63-SNAPSHOT
sbt 'set ThisBuild / version := "0.0.63-SNAPSHOT"' 'publish'
git push origin HEAD --tags
```