"""Generate the synthetic toy dataset used as a POUTINE smoke/golden fixture.

Usage: python gen_toy_dataset.py [out_dir]   (default: current directory)

The committed files are the source of truth. Python's random module is not
guaranteed to reproduce the same stream across Python versions, so this script
is kept for provenance, not for regenerating the fixture.
"""
import os
import random
import sys
out = sys.argv[1] if len(sys.argv) > 1 else '.'
os.makedirs(out, exist_ok=True)
random.seed(1)
N=40; L=300
# random binary tree via random merging
nodes=[{'name':f's{i}','kids':[],'bl':0} for i in range(N)]
pool=list(nodes); k=0
while len(pool)>1:
    a,b=random.sample(pool,2); pool.remove(a); pool.remove(b)
    n={'name':None,'kids':[a,b],'bl':0}; pool.append(n)
root=pool[0]
def assign(n):
    for c in n['kids']:
        c['bl']=random.uniform(0.02,0.3); assign(c)
assign(root)
def nw(n):
    if not n['kids']: return f"{n['name']}:{n['bl']:.5f}"
    return "("+",".join(nw(c) for c in n['kids'])+f"):{n['bl']:.5f}"
open(os.path.join(out,'tree.nwk'),'w').write(nw(root)+";\n")
alleles=[random.choice([('A','G'),('C','T')]) for _ in range(L)]
seqs={f's{i}':[] for i in range(N)}
rate=1.0
for a,b in alleles:
    def sim(n,st):
        for c in n['kids']:
            s=st
            # number of mutations ~ branch length * rate scaled
            if random.random()<c['bl']*rate*0.8: s=b if st==a else a
            c['st']=s; sim(c,s)
    root['st']=a; sim(root,a)
    leaves=[]
    def collect(n):
        if not n['kids']: leaves.append(n)
        for c in n['kids']: collect(c)
    collect(root)
    for l in leaves: seqs[l['name']].append(l['st'])
# keep only variable sites
keep=[j for j in range(L) if len({seqs[s][j] for s in seqs})>1]
with open(os.path.join(out,'sites.fa'),'w') as f:
    for s,v in seqs.items(): f.write(f">{s}\n{''.join(v[j] for j in keep)}\n")
with open(os.path.join(out,'sites.map'),'w') as f:
    for i,j in enumerate(keep): f.write(f"1\tsnp{i}\t0\t{(j+1)*100}\n")
with open(os.path.join(out,'phenos.txt'),'w') as f:
    for i in range(N): f.write(f"s{i}\t{random.randint(0,1)}\n")
print(len(keep),"variable sites")
