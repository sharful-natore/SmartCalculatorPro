# -*- coding: utf-8 -*-
import json

with open('app/src/main/assets/dictionary_1000.json') as f:
    d = json.load(f)

existing = set(w['word'].strip().lower() for w in d)
print("Existing count:", len(existing))

test_candidates = [
    # Literary & Rhetorical devices
    'Catachresis', 'Anacoluthon', 'Aposiopesis', 'Zeugma', 'Chiasmus', 'Asyndeton',
    'Polysyndeton', 'Epistrophe', 'Anaphora', 'Paraprosdokian', 'Hypallage', 'Tmesis',
    'Anastrophe', 'Antiphrasis', 'Hendiadys', 'Syllepsis', 'Paronomasia', 'Anthimeria',
    'Parataxis', 'Hypotaxis', 'Apostrophe', 'Epanalepsis', 'Anadiplosis', 'Polyptoton',
    'Antimetabole', 'Epizeuxis', 'Isocolon', 'Tricolon', 'Chiasm', 'Kenning',
    'Meiosis', 'Pleonasm', 'Synesthesia', 'Oxymoron', 'Periphrasis', 'Dysphemism',
    
    # Archetypes, character traits & person types
    'Poltroon', 'Braggadocio', 'Rodomont', 'Scaramouch', 'Pantaloon', 'Harlequin',
    'Pierrot', 'Mountebank', 'Saltimbanco', 'Quacksalver', 'Medicaster', 'Poetaster',
    'Criticaster', 'Grammaticaster', 'Sciolist', 'Sophiology', 'Opsimath', 'Plutocrat',
    'Kleptocrat', 'Ochlocrat', 'Kakistocrat', 'Gerontocrat', 'Theocrat', 'Autocrat',
    'Oligarch', 'Demagogue', 'Patrician', 'Plebeian', 'Parvenu', 'Nouveau riche',
    'Churl', 'Misocapnic', 'Graphomaniac', 'Bibliomaniac', 'Ergophile', 'Ergophobe',
    'Neophile', 'Neophobe', 'Xenophile', 'Monophobe', 'Claustrophobe', 'Agoraphobe',
    
    # Human qualities, quirks & states
    'Peccadillo', 'Foible', 'Crotchet', 'Vagary', 'Caprice', 'Idiosyncrasy',
    'Ataraxia', 'Apatheia', 'Eudaimonia', 'Anomie', 'Weltschmerz', 'Sehnsucht',
    'Saudade', 'Schadenfreude', 'Akrasia', 'Hamartia', 'Anagnorisis', 'Peripeteia',
    'Catharsis', 'Bathos', 'Quietism', 'Solipsism', 'Fideism', 'Nominalism',
    'Monism', 'Dualism', 'Pluralism', 'Deism', 'Pantheism', 'Panentheism',
    'Agnosticism', 'Gnosticism', 'Antinomianism', 'Pelagianism', 'Jansenism',
    
    # Eloquence, speech, argumentation
    'Eristic', 'Dialectic', 'Casuistry', 'Sophism', 'Paralogism', 'Syllogism',
    'Enthymeme', 'Sorites', 'Aporia', 'Antinomy', 'Axiom', 'Postulate',
    'Corroboration', 'Refutation', 'Confutation', 'Disquisition', 'Tractate',
    'Exegesis', 'Hermeneutics', 'Glossary', 'Lexicography', 'Philology',
    'Paleography', 'Epigraphy', 'Papyrology', 'Codicology', 'Sigillography',
    'Vexillology', 'Numismatist', 'Cartography', 'Topography', 'Chorography',
    'Chronology', 'Horology', 'Dendrochronology', 'Stratigraphy', 'Petrology',
    'Speleology', 'Volcanology', 'Seismology', 'Limnology', 'Glaciology',
    'Meteorology', 'Climatology', 'Aerology', 'Mycology', 'Lichenology',
    'Pteridology', 'Dendrology', 'Pomology', 'Agrostology', 'Ornithology',
    'Herpetology', 'Ichthyology', 'Malacology', 'Conchology', 'Carcinology',
    'Entomology', 'Myrmecology', 'Lepidopterology', 'Coleopterology', 'Melittology',
    'Arachnology', 'Acarology', 'Helminthology', 'Nematology', 'Protozoology',
    'Virology', 'Bacteriology', 'Immunology', 'Serology', 'Epidemiology',
    'Toxicology', 'Pharmacology', 'Posology', 'Etiology', 'Nosology',
    'Symptomatology', 'Syndromic', 'Pathophysiology', 'Cytopathology', 'Histopathology',
    'Gerontology', 'Pediatrics', 'Obstetrics', 'Gynecology', 'Neonatology'
]

fresh = [w for w in test_candidates if w.lower() not in existing]
print(f"Total test candidates: {len(test_candidates)}, Fresh candidates: {len(fresh)}")
with open('fresh_pool.json', 'w') as out:
    json.dump(fresh, out, indent=2)
