# simplebox

## introduce

`simplebox` is a tiny container to manager java app lifetime.

It just support to let your app run. No http. No tcp. All of these should be developed by yourself.

## howto

1. set ENV <SBOX_HOME> to the simplebox's dir
1. put your app to <SBOX_HOME>/app. All of classes should be in directory named `classes`. All of jars should be in directory named `lib`.
1. create a property file who named `boot.properties`. input following content:
```
BootClass=DemoStarter
BootMethod=init
StopClass=DemoStopper
StopMethod=shutdown
```

Now, run start.bat/start.sh. To stop, run stop.bat/stop.sh.
