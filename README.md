<p align="center"><img src="docs/logo.svg" width="96" alt="" /></p>

# fecsmith

fecsmith is an offline command-line tool for embedded and protocol developers and students of coding theory who need test vectors for error-detecting and error-correcting codes. It computes CRCs, encodes and decodes extended Hamming, Reed-Solomon and convolutional codes, and analyzes code behavior.

[![CI](https://github.com/Gulliby-development/fecsmith/actions/workflows/ci.yml/badge.svg)](https://github.com/Gulliby-development/fecsmith/actions/workflows/ci.yml) [![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

## Why

Reach for fecsmith when you need a known-good test vector for a CRC, a Hamming, Reed-Solomon or convolutional codeword, or want to see how a code behaves under errors, without installing anything beyond Java 21. It prints plain text identically on Windows, macOS and Linux.

## Install

Install Java 21 and Maven. Build the application and run its tests with:

```sh
mvn -B verify
```

Run a command with:

```sh
java -cp target/classes io.github.gulliby.fecsmith.Main COMMAND
```

In the examples below, `fecsmith` is shorthand for that Java launch command with the requested command and arguments appended.

## Usage

```text
usage: fecsmith crc [--list] MODEL HEX...
       fecsmith secded encode|decode BITS
       fecsmith rs encode|decode [--m M] [--nsym N] SYMBOL...
       fecsmith conv encode|decode CODE BITS
       fecsmith weights K
       fecsmith outcomes [--errors N] K
       fecsmith burst [--max-len N] MODEL
       fecsmith dfree CODE
```

`fecsmith --version` prints the program name and its version on one line, such as `fecsmith 0.1.0`, and exits with 0.

## Commands

- `crc` computes a CRC for hexadecimal input using one of 13 named models or a custom model specification; `--list` displays the catalog.
- `secded` encodes data with extended Hamming codes or decodes a codeword, correcting a single-bit error and reporting detected double-bit or uncorrectable errors.
- `rs` encodes or decodes symbols using Reed-Solomon codes over GF(2^m), reporting corrected errors or an uncorrectable word.
- `conv` encodes bits or decodes received bits with the supported rate-1/2 convolutional codes `k3`, `k5` and `k7`.
- `weights` reports the weight distribution and minimum distance of an extended Hamming code for the requested data size.
- `outcomes` counts corrected, detected and wrong outcomes for SECDED words with each requested number of flipped bits.
- `burst` counts CRC-undetected burst error patterns through the selected maximum length.
- `dfree` finds and reports the free distance and a corresponding path for a convolutional code.

### CRC

Command:

```sh
fecsmith crc crc32 313233343536373839
```

Output:

```text
crc32 over 9 bytes: cbf43926
```

### SECDED

Command:

```sh
fecsmith secded decode 0010001100
```

Output:

```text
secded k=5 r=4 n=10
syndrome  3
parity    odd
status    corrected bit 3
codeword  0011001100
data      10110
```

### Reed-Solomon

Command:

```sh
fecsmith rs decode 48 00 6c 6c ff 08 47 90 ca
```

Output:

```text
RS(9,5) over GF(2^8), t=2
syndrome  05 5a a0 6c
status    corrected 2 errors
errors    1:65 4:90
codeword  48 65 6c 6c 6f 08 47 90 ca
message   48 65 6c 6c 6f
```

### More examples

Each block shows the command after `$` and its output.

CRC with a custom model:

```console
$ fecsmith crc crc:5:15:00:0:0:00 313233343536373839
crc:5:15:00:0:0:00 over 9 bytes: 1c
```

SECDED encode:

```console
$ fecsmith secded encode 10110
secded k=5 r=4 n=10
data      10110
codeword  0011001100
```

Reed-Solomon encode:

```console
$ fecsmith rs encode 48 65 6c 6c 6f
RS(9,5) over GF(2^8), t=2
codeword  48 65 6c 6c 6f 08 47 90 ca
parity    08 47 90 ca
```

Convolutional encode:

```console
$ fecsmith conv encode k3 1011
conv k3 (K=3, generators 7 and 5 octal)
input     1011
coded     11 10 00 01 01 11
```

Convolutional decode:

```console
$ fecsmith conv decode k3 111100010111
conv k3 (K=3, generators 7 and 5 octal)
status    corrected 1 channel bit error
message   1011
```

Weight distribution:

```console
$ fecsmith weights 4
secded k=4 r=3 n=8, minimum distance 4
weight  codewords
0       1
4       14
8       1
```

Error outcomes:

```console
$ fecsmith outcomes 5
secded k=5 r=4 n=10
errors  patterns  corrected  detected  wrong
1       10        10         0         0
2       45        0          45        0
3       120       0          48        72
```

CRC burst analysis:

```console
$ fecsmith burst --max-len 10 crc8
crc8, width 8, bursts up to length 10
length  patterns  undetected
1       1         0
2       1         0
3       2         0
4       4         0
5       8         0
6       16        0
7       32        0
8       64        0
9       128       1
10      256       1
```

Free distance:

```console
$ fecsmith dfree k3
k3 (K=3, generators 7 and 5 octal): free distance 5
input     100
coded     11 10 11
```

## Exit codes

- `0`: the command succeeded.
- `1`: a decoding command returned a negative answer, such as a detected double error or uncorrectable SECDED or Reed-Solomon word.
- `2`: command usage failed; a diagnostic is written to standard error and standard output is empty.

## Development

Run the full build and test suite with:

```sh
mvn -B verify
```

## License

MIT, see [LICENSE](LICENSE).

---

Built with BAKED.
