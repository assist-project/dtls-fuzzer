#!/usr/bin/env python3

import argparse
import copy
import sys
import xml.etree.ElementTree as ET


def entity_identity(elem):
    name = elem.attrib.get("name")

    if name is not None:
        return (elem.tag, name)

    return (
        elem.tag,
        tuple(sorted(elem.attrib.items()))
    )

def load_alphabet(path):
    tree = ET.parse(path)
    root = tree.getroot()

    if root.tag != "alphabet":
        raise ValueError(f"{path} does not contain an <alphabet> root element")

    return root

def entity_sort_key(elem):
    return (
        elem.tag,
        elem.attrib.get("name", "")
    )


def sort_alphabet(root):
    """
    Sort:
        1. Non-RAOutputSymbol entities
        2. RAOutputSymbol entities

    Within each group:
        1. Entity tag name
        2. Value of the name attribute
    """

    non_ra = [copy.deepcopy(e) for e in root if e.tag != "RAOutputSymbol"]
    ra = [copy.deepcopy(e) for e in root if e.tag == "RAOutputSymbol"]

    non_ra.sort(key=entity_sort_key)
    ra.sort(key=entity_sort_key)

    new_root = ET.Element("alphabet")

    for elem in non_ra:
        new_root.append(elem)

    for elem in ra:
        new_root.append(elem)

    return new_root


def remove_duplicates(root):
    """
    Remove duplicates based on the 'name' attribute.
    Keeps the first occurrence.
    """

    new_root = ET.Element("alphabet")
    seen = set()
    
    for elem in root:
        identity = entity_identity(elem)
    
        if identity in seen:
            continue
    
        seen.add(identity)
        new_root.append(copy.deepcopy(elem))
    
    return new_root


def merge_alphabets(root1, root2):
    """
    Merge two alphabets.

    Duplicate definition:
        same name attribute

    Keeps entity from first alphabet.
    """

    new_root = ET.Element("alphabet")
    seen = set()

    for root in (root1, root2):
        for elem in root:
            identity = entity_identity(elem)

            if identity in seen:
                continue
            
            seen.add(identity)
            new_root.append(copy.deepcopy(elem))

    new_root = sort_alphabet(new_root)

    return new_root


def entity_key(elem):
    """
    Used for merge-outputs comparisons.
    """

    return (
        elem.tag,
        tuple(sorted(elem.attrib.items()))
    )


def compare_non_ra_entities(root1, root2):
    set1 = {
        entity_key(e)
        for e in root1
        if e.tag != "RAOutputSymbol"
    }

    set2 = {
        entity_key(e)
        for e in root2
        if e.tag != "RAOutputSymbol"
    }

    return set1 - set2, set2 - set1


def print_entity_set(title, entries):
    print(title)

    for tag, attrs in sorted(entries):
        attr_str = " ".join(
            f'{k}="{v}"'
            for k, v in attrs
        )
        print(f"  <{tag} {attr_str}/>")


def merge_outputs(root1, root2):
    """
    Original behavior:

    1. Verify all non-RAOutputSymbol entities match.
    2. Print differences and exit otherwise.
    3. Merge all entities.
    4. Remove duplicate names.
    5. Sort output.
    """

    only_in_1, only_in_2 = compare_non_ra_entities(root1, root2)

    if only_in_1 or only_in_2:
        print("Non-RAOutputSymbol entities differ.\n")

        if only_in_1:
            print_entity_set(
                "Present only in first alphabet:",
                only_in_1
            )

        if only_in_2:
            print()
            print_entity_set(
                "Present only in second alphabet:",
                only_in_2
            )

        sys.exit(1)

    merged = merge_alphabets(root1, root2)

    return merged


def write_alphabet(root, output_path=None):
    tree = ET.ElementTree(root)

    try:
        ET.indent(tree, space="    ")
    except AttributeError:
        pass

    if output_path:
        tree.write(
            output_path,
            encoding="utf-8",
            xml_declaration=True
        )
    else:
        import io

        buffer = io.BytesIO()

        tree.write(
            buffer,
            encoding="utf-8",
            xml_declaration=True
        )

        print(buffer.getvalue().decode("utf-8"))


def cmd_sort(args):
    root = load_alphabet(args.input)
    result = sort_alphabet(root)
    write_alphabet(result, args.output)


def cmd_dedup(args):
    root = load_alphabet(args.input)
    result = remove_duplicates(root)
    result = sort_alphabet(result)
    write_alphabet(result, args.output)


def cmd_merge(args):
    root1 = load_alphabet(args.input1)
    root2 = load_alphabet(args.input2)

    result = merge_alphabets(root1, root2)
    write_alphabet(result, args.output)


def cmd_merge_outputs(args):
    root1 = load_alphabet(args.input1)
    root2 = load_alphabet(args.input2)

    result = merge_outputs(root1, root2)
    write_alphabet(result, args.output)


def main():
    parser = argparse.ArgumentParser(
        description="Alphabet Manager"
    )

    subparsers = parser.add_subparsers(dest="command")
    subparsers.required = True

    p = subparsers.add_parser("sort")
    p.add_argument("-i", "--input", required=True)
    p.add_argument("-o", "--output", help="Output file. If omitted, write XML to stdout.")
    p.set_defaults(func=cmd_sort)

    p = subparsers.add_parser("dedup")
    p.add_argument("-i", "--input", required=True)
    p.add_argument("-o", "--output", help="Output file. If omitted, write XML to stdout.")
    p.set_defaults(func=cmd_dedup)

    p = subparsers.add_parser("merge")
    p.add_argument("-i", "--input1", required=True)
    p.add_argument("-j", "--input2", required=True)
    p.add_argument("-o", "--output", help="Output file. If omitted, write XML to stdout.")
    p.set_defaults(func=cmd_merge)

    p = subparsers.add_parser("merge-outputs")
    p.add_argument("-i", "--input1", required=True)
    p.add_argument("-j", "--input2", required=True)
    p.add_argument("-o", "--output", help="Output file. If omitted, write XML to stdout.")
    p.set_defaults(func=cmd_merge_outputs)

    args = parser.parse_args()
    args.func(args)


if __name__ == "__main__":
    main()