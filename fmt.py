import sys, json
data = json.load(sys.stdin)
print(json.dumps(data, indent=2, ensure_ascii=True))
