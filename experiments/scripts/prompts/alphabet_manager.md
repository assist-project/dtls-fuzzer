> Could you write a script which compares symbol entities within the alphabet XML entity. It checks that all symbol entities excluding RAOutputSymbol that are present in one file are also present in the other. If that is not the case, it prints out entities included in one file, and not the other. If that is the case, then it merges the two files into one XML file, which has an alphabet entity containing all unique symbol entities from both files.

> Could you update this to sort the RAOutputSymbol entities in the combined alphabet file based on their name?

> Could you further update the script so it becomes an alphabet manager. As such, it should support the following functionality. (1) Sorting an alphabet, separately sorting RAOutputSymbol entities, and the non-RAOutputSymbol entities. The non-RAOutputSymbol entities should come first. (2) Removing duplicate entities from an alphabet, where an entity is identical to another if their names (given by the name attribute) are the same(3) Merging two alphabets, the result should be a sorted alphabet with no duplicate entries. If the two alphabets contain an identical entity, keep the entity of the first(4) Merging two alphabets, as was done before, you could call this merge outputs.Arguments include:- paths to the alphabets supplied as inputs and to be gneerated as output

> Can you update the script such that if the option '-o' is not given, the alphabet is printed to STDOUT?

> also, update so that sorting is done first by the name of the entity, then by the value of the name attribute
