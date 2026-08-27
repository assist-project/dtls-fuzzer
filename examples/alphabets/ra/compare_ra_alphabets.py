#Could you write a script which compares symbol entities within the alphabet XML entity. It checks that all symbol entities excluding RAOutputSymbol that are present in one file are also present in the other. If that is not the case, it prints out entities included in one file, and not the other. If that is the case, then it merges the two files into one XML file, which has an alphabet entity containing all unique symbol entities from both files.
#
#Yes. Based on the two XML files you provided, the script can:
#
#Parse the <alphabet> element from both files.
#Compare all symbol entities except RAOutputSymbol.
#If a symbol entity exists in one file but not the other:
#Print entities unique to each file.
#Exit without creating a merged file.
#If all non-RAOutputSymbol entities match:
#Merge both alphabets.
#Keep all unique symbol entities from both files.
#Write a new XML file.

#!/usr/bin/env python3

import sys
import xml.etree.ElementTree as ET
from collections import OrderedDict
from copy import deepcopy


def entity_key(elem):
    """
    Create a comparable key for an XML entity.
    RAOutputSymbols are excluded from comparison.
    """
    attrs = tuple(sorted(elem.attrib.items()))
    return (elem.tag, attrs)


def load_alphabet(filename):
    tree = ET.parse(filename)
    root = tree.getroot()

    if root.tag != "alphabet":
        raise ValueError(f"{filename} does not contain an <alphabet> root element")

    return tree, root


def compare_non_ra_symbols(root1, root2):
    symbols1 = {
        entity_key(e)
        for e in root1
        if e.tag != "RAOutputSymbol"
    }

    symbols2 = {
        entity_key(e)
        for e in root2
        if e.tag != "RAOutputSymbol"
    }

    only_in_1 = symbols1 - symbols2
    only_in_2 = symbols2 - symbols1

    return only_in_1, only_in_2

def merge_alphabets(root1, root2):
    merged = ET.Element("alphabet")

    seen = set()
    non_ra_symbols = []
    ra_symbols = []

    for root in (root1, root2):
        for elem in root:
            key = (
                elem.tag,
                tuple(sorted(elem.attrib.items()))
            )

            if key in seen:
                continue

            seen.add(key)

            if elem.tag == "RAOutputSymbol":
                ra_symbols.append(deepcopy(elem))
            else:
                non_ra_symbols.append(deepcopy(elem))

    # Add non-RAOutputSymbol entities in original order
    for elem in non_ra_symbols:
        merged.append(elem)

    # Sort RAOutputSymbol entities by name
    ra_symbols.sort(key=lambda e: e.attrib.get("name", ""))

    for elem in ra_symbols:
        merged.append(elem)

    return merged


def print_entities(title, entities):
    print(title)
    for tag, attrs in sorted(entities):
        attr_str = " ".join(f'{k}="{v}"' for k, v in attrs)
        print(f"  <{tag} {attr_str}/>")


def main(file1, file2, output_file):
    tree1, root1 = load_alphabet(file1)
    tree2, root2 = load_alphabet(file2)

    only_in_1, only_in_2 = compare_non_ra_symbols(root1, root2)

    if only_in_1 or only_in_2:
        print("Non-RAOutputSymbol entities differ.\n")

        if only_in_1:
            print_entities(f"Present only in {file1}:", only_in_1)

        if only_in_2:
            print()
            print_entities(f"Present only in {file2}:", only_in_2)

        sys.exit(1)

    merged_root = merge_alphabets(root1, root2)

    merged_tree = ET.ElementTree(merged_root)
    ET.indent(merged_tree, space="    ")
    merged_tree.write(output_file, encoding="utf-8", xml_declaration=True)

    print(f"Merged alphabet written to: {output_file}")


if __name__ == "__main__":
    if len(sys.argv) != 4:
        print(
            f"Usage: {sys.argv[0]} file1.xml file2.xml merged.xml"
        )
        sys.exit(1)

    main(sys.argv[1], sys.argv[2], sys.argv[3])
