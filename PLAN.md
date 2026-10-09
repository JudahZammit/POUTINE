 Line 824 treetime ancestral is called. Add "--rng-seed RNG_SEED   random number generator seed for treetime" param from global RNG_SEED
 Check if fisher_exact_R is used. I don't think it is. If it is do we need rng seeding
 Also check qvalues (line 5224)

 On lines 4911 and lines 6014-6015, seed the random number generator with Random rnd = New Random(global_seed)