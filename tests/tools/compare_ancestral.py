"""Compare two treetime `ancestral` output directories.

Usage: python compare_ancestral.py <dir_A> <dir_B> <label>

Reports, for the two directories:
  - whether the sequence names match, and how many leaf and internal nodes differ in their sequences
  - whether the annotated trees have the same node names and order, and the largest branch-length difference
  - whether sequence_evolution_model.txt is textually identical

Used for the treetime version comparison; see docs/treetime-version-comparison.md.
"""
import sys,re,os
def read_fasta(p):
    d={};n=None
    for l in open(p):
        l=l.rstrip('\n')
        if l.startswith('>'): n=l[1:].strip(); d[n]=[]
        elif n is not None: d[n].append(l.strip())
    return {k:''.join(v) for k,v in d.items()}
def tree_str(p):
    s=open(p).read(); m=re.search(r'=\s*(\[&R\])?\s*(\(.*;)',s,re.S); return m.group(2) if m else None
def strip_comments(t): return re.sub(r'\[[^\]]*\]','',t)
def nodes_branch(t):
    # (name,branchlength) for every node in order of appearance
    t=strip_comments(t); out=[]
    for m in re.finditer(r'([A-Za-z0-9_\.\-\|]+)?(?::([0-9eE\.\-\+]+))',t): out.append((m.group(1),float(m.group(2))))
    return out
a,b,label=sys.argv[1],sys.argv[2],sys.argv[3]
fa,fb=read_fasta(a+'/ancestral_sequences.fasta'),read_fasta(b+'/ancestral_sequences.fasta')
ka,kb=set(fa),set(fb)
print(f"[{label}] sequences: {len(fa)} vs {len(fb)}; names only in A: {len(ka-kb)}, only in B: {len(kb-ka)}")
common=sorted(ka&kb); leaves=[k for k in common if not k.startswith('NODE_')]; inner=[k for k in common if k.startswith('NODE_')]
def cmp(names):
    nd=0; sd=0; L=0
    for k in names:
        x,y=fa[k],fb[k]; L=len(x)
        if len(x)!=len(y): nd+=1; sd+=abs(len(x)-len(y)); continue
        d=sum(1 for i,j in zip(x,y) if i!=j)
        if d: nd+=1; sd+=d
    return nd,sd,len(names),L
for nm,names in (("leaf",leaves),("internal",inner)):
    nd,sd,n,L=cmp(names); print(f"   {nm} nodes: {n} compared, {nd} differ, {sd} differing site-states of {n*L}")
ta,tb=tree_str(a+'/annotated_tree.nexus'),tree_str(b+'/annotated_tree.nexus')
if ta and tb:
    na,nb=nodes_branch(ta),nodes_branch(tb)
    same_names=[x[0] for x in na]==[x[0] for x in nb]
    mx=max((abs(x[1]-y[1]) for x,y in zip(na,nb)),default=None) if len(na)==len(nb) else None
    print(f"   tree: {len(na)} vs {len(nb)} branches; same node names/order: {same_names}; max |branch length diff|: {mx}")
ma,mb=open(a+'/sequence_evolution_model.txt').read(),open(b+'/sequence_evolution_model.txt').read()
print("   substitution model identical text:",ma==mb)
