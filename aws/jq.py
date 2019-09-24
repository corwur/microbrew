import json, sys

def idx(d,l):
    if not l:
        return ""
    else:
        head, tail = l[0], l[1:]
        if not tail:
            return d[head]
        else:
            return idx(d[head],tail)

fields = sys.argv[1:]
data = json.load(sys.stdin)
value = idx(data,fields)
print(value)