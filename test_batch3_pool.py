# -*- coding: utf-8 -*-
import json

with open('app/src/main/assets/dictionary_1000.json') as f:
    d = json.load(f)
existing = set(w['word'].strip().lower() for w in d)
with open('fresh_pool.json') as f:
    fresh1 = json.load(f)
for w in fresh1:
    existing.add(w.strip().lower())
with open('fresh_pool2.json') as f:
    fresh2 = json.load(f)
for w in fresh2:
    existing.add(w.strip().lower())

cands3 = [
    # Ecology, Biology & Nature
    'Orographic', 'Riparian', 'Littoral', 'Estuarine', 'Brackish', 'Pelagic',
    'Benthic', 'Abyssal', 'Hadal', 'Neritic', 'Planktonic', 'Nektonic',
    'Epiphytic', 'Saprophytic', 'Xerophytic', 'Hydrophytic', 'Halophytic',
    'Mesophytic', 'Coniferous', 'Arboreal', 'Fossorial', 'Cursorial',
    'Volant', 'Saltatorial', 'Natatorial', 'Estivation', 'Brumation',
    'Torpor', 'Cryptobiosis', 'Diapause', 'Phenology', 'Taxidermy',
    'Phylogeny', 'Ontogeny', 'Speciation', 'Sympatric', 'Allopatric',
    'Endemism', 'Biomass', 'Biosphere', 'Bioluminescence', 'Bioaccumulation',
    'Biomagnification', 'Eutrophication', 'Bioremediation', 'Symbiosis',
    'Mutualism', 'Commensalism', 'Parasitism', 'Amensalism',
    
    # Philosophy, Logic, Psychology & Cognition
    'Teleology', 'Axiology', 'Deontology', 'Consequentialism',
    'Individuation', 'Synchronicity', 'Sublimation', 'Introjection',
    'Heuristic', 'Abductive', 'Petitio principii', 'Tu quoque',
    'Reductio ad absurdum', 'A posteriori', 'A priori', 'Tabula rasa',
    'Categorical imperative', 'Dialectical materialism', 'Historical materialism',
    'Determinism', 'Indeterminism', 'Fatalism', 'Compatibilism', 'Incompatibilism',
    'Solipsistic', 'Solipsism', 'Epiphenomenon', 'Qualia', 'Panpsychism',
    'Behaviorism', 'Gestalt', 'Cognitive dissonance',
    
    # Rare high-impact English words from exams & literature
    'Bile', 'Choler', 'Phlegmatic', 'Sanguineous', 'Bilious', 'Atrabilious',
    'Splenetic', 'Saturnine', 'Mercurial', 'Jovial', 'Martial', 'Apollonian',
    'Dionysian', 'Promethean', 'Sisyphean', 'Tantalizing', 'Procrustean',
    'Protean', 'Gordian', 'Pyrrhic', 'Draconian', 'Spartan', 'Utopian',
    'Dystopian', 'Machiavellian', 'Orwellian', 'Kafkaesque', 'Byronic',
    'Faustian', 'Quixotic', 'Pickwickian', 'Micawberish', 'Pecksniffian',
    'Gargantuan', 'Panglossian', 'Malapropian', 'Babbittry', 'Bowdlerism',
    'Comstockery', 'Grundyism', 'Philistinism', 'Vandalism', 'Iconoclasm'
]

fresh3 = [c.strip() for c in cands3 if c.strip().lower() not in existing]
print(f"Cand3 total: {len(cands3)}, Fresh3: {len(fresh3)}")
with open('fresh_pool3.json', 'w') as out:
    json.dump(fresh3, out, indent=2)

total_fresh = len(fresh1) + len(fresh2) + len(fresh3)
print(f"Total accumulated fresh words across pool 1, 2, 3: {total_fresh}")
