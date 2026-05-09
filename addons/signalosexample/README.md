# SignalOS Example Addon

This module shows three SignalOS integration paths:

- Java registration in `SignalOsExample`.
- Datapack JSON under `data/signalosexample/signalos`.
- KubeJS-friendly usage through `SignalOSKubeBridge`.

## Included Content

- Java chapter: `signalosexample:java_ops`
- Java mission: `signalosexample:java_boot`
- Java diagnostics provider: `signalosexample:example_diagnostics`
- JSON chapter: `signalosexample:field_ops`
- JSON mission: `signalosexample:secure_cache`
- JSON archive: `signalosexample:field_ops_brief`

The JSON mission uses `minecraft:story/root` as its completion advancement so it can be completed quickly in a normal test world.

## Java API Shape

```java
SignalOsApi.registerChapter(TerminalChapter.builder("signalosexample:java_ops")
        .title("Java Ops")
        .section("system")
        .page("missions")
        .page("archives")
        .page("diagnostics")
        .build());
```

## KubeJS-Friendly Script Shape

This is a soft bridge loaded through `Java.loadClass`, not a native KubeJS plugin event.

```js
const SignalOSEvents = Java.loadClass('com.knoxhack.signalos.kubejs.SignalOSEvents')

ServerEvents.loaded(event => {
  SignalOSEvents.content(event => {
    event.clear()

    event.chapter('signalosexample:script_ops')
      .title('Script Ops')
      .section('progress')
      .page('missions')
      .register()

    event.archive('signalosexample:script_brief')
      .chapter('signalosexample:script_ops')
      .title('Script Brief')
      .line('This content was registered from a KubeJS script.')
      .register()
  })
})
```

For reloadable pack content, prefer placing equivalent JSON files under the KubeJS `data/` folder.
