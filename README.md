# Joy Of Clojure Examples

This repository includes sample code (and notes) from
[The Joy of Clojure][joy-of-clojure], and serves as a companion to the
book. This repository exists because I find that typing the book's
examples into a file and executing the resultant forms via SLIME is
more flexible than typing the examples directly into the Clojure REPL.

That being said, this is probably not what you're looking for. *The
source code that accompanies the book is
[available directly from the authors][joy-of-clojure-source].*

## Usage

Install [swank-clojure][].

From within this project, invoke `M-x clojure-jack-in`.

For reference (from the [swank-clojure][] README), commonly-used SLIME
commands include:

* **M-.**: Jump to the definition of a var
* **C-c TAB**: Autocomplete symbol at point
* **C-x C-e**: Eval the form under the point
* **C-c C-k**: Compile the current buffer
* **C-c C-l**: Load current buffer and force required namespaces to reload
* **C-M-x**: Compile the whole top-level form under the point.
* **C-c S-i**: Inspect a value
* **C-c C-m**: Macroexpand the call under the point
* **C-c C-d C-d**: Look up documentation for a var
* **C-c C-z**: Switch from a Clojure buffer to the repl buffer
* **C-c M-p**: Switch the repl namespace to match the current buffer
* **C-c C-w c**: List all callers of a given function

Pressing "v" on a stack trace a debug buffer will jump to the file and line
referenced by that frame if possible.

[joy-of-clojure]: http://joyofclojure.com/
[joy-of-clojure-source]: https://github.com/joyofclojure/book-source
[swank-clojure]: https://github.com/technomancy/swank-clojure

