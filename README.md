# doomsday-metrics

A way to collect and display metrics in the terminal when all other monitoring systems are down.

## Usage

- Download and extract [the executable](https://github.com/gchudnov/doomsday-metrics/releases).
- Prepare html-queries or shell scripts that extract metrics.
- Prepare a JSON-file with configuration that are using the created html query or shell scripts.
- Run application from the command line:

```bash
./doom ./path-to-config.json
```

## Command-line parameters

```text
  ./doom --help

TODO
```


## Building

```sbt
doom/assembly
```
