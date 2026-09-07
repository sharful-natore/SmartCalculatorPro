# -*- coding: utf-8 -*-
import json

with open('app/src/main/assets/dictionary_1000.json') as f:
    d = json.load(f)
existing = set(w['word'].strip().lower() for w in d)
with open('fresh_pool.json') as f:
    fresh1 = json.load(f)
for w in fresh1:
    existing.add(w.strip().lower())

cands2 = [
    # Latin legal & scholarly phrases in English
    'Jurisprudence', 'Mandamus', 'Certiorari', 'Habeas corpus', 'Res judicata',
    'Subpoena', 'Affidavit', 'Amicus curiae', 'De jure', 'De facto',
    'Ex post facto', 'Bona fide', 'Mens rea', 'Actus reus', 'Nolo contendere',
    'Caveat emptor', 'Prima facie', 'Non sequitur', 'Ipso facto', 'Modus operandi',
    'Modus vivendi', 'Sine qua non', 'Ultra vires', 'Inter alia', 'Mutatis mutandis',
    'Status quo', 'Stare decisis', 'In loco parentis', 'Pro bono', 'Quid pro quo',
    'Force majeure', 'Locus standi', 'Sub judice', 'In absentia', 'Ex parte',
    'Obiter dictum', 'Ratio decidendi', 'Casus belli', 'Tabula rasa', 'Mea culpa',
    'Persona non grata', 'Vox populi', 'Status quo ante', 'Ad infinitum',

    # Architecture, Art & Spatial aesthetics
    'Chiaroscuro', 'Sfumato', 'Pentimento', 'Tenebrism', 'Grisaille', 'Trompe-l\'oeil',
    'Bas-relief', 'Fresco', 'Impasto', 'Arabesque', 'Grotesque', 'Gargoyle',
    'Flying buttress', 'Balustrade', 'Architrave', 'Frieze', 'Cornice', 'Colonnade',
    'Portico', 'Rotunda', 'Cupola', 'Spire', 'Minaret', 'Nave', 'Transept',
    'Apse', 'Chancel', 'Clerestory', 'Pergola', 'Gazebo', 'Kiosk', 'Pavilion',
    'Parapet', 'Battlement', 'Rampart', 'Barbican', 'Portcullis', 'Palisade',
    'Stockade', 'Bastion', 'Citadel', 'Redoubt', 'Donjon', 'Casemate', 'Turret',
    'Crenellation', 'Merlon', 'Machicolation', 'Glacis', 'Escarpment',

    # High-level adjectives & nouns of discourse, critique, style
    'Vituperative', 'Calumnious', 'Ignominious', 'Contumelious', 'Opprobrious',
    'Pejorative', 'Invective', 'Philippic', 'Diatribe', 'Harangue', 'Tirade',
    'Screed', 'Jeremiad', 'Pasquinade', 'Lampoon', 'Travesty', 'Burlesque',
    'Pastiche', 'Innuendo', 'Insinuation', 'Insinuative', 'Trenchant', 'Incisive',
    'Mordant', 'Caustic', 'Scathing', 'Vitriolic', 'Acerbic', 'Acidulous',
    'Mordacious', 'Astringent', 'Pungent', 'Piquant', 'Zesty', 'Sapid', 'Palatable',
    'Toothsome', 'Gustatory', 'Olfactory', 'Haptic', 'Vestibular', 'Proprioceptive',
    'Kinaesthetic',

    # Behavioral, personality, ethical & moral concepts
    'Sciolism', 'Megalopsychia', 'Eudemonic', 'Ataraxic', 'Autochthonous',
    'Brobdingnagian', 'Lilliputian', 'Gargantuan', 'Panglossian', 'Maladroit',
    'Adroit', 'Deftness', 'Nimbleness', 'Celerity', 'Alacrity', 'Expedition',
    'Promptitude', 'Tardiness', 'Dilatoriness', 'Procrastinative', 'Temporization',
    ' tergiversator', 'Equivocator', 'Prevaricator', 'Fabricator', 'Dissembler',
    'Hypocrite', 'Sanctimoniousness', 'Pharisaism', 'Tartuffery', 'Cant', 'Humbug',
    'Charlatanry', 'Empiricism', 'Dogmatism', 'Pragmatism', 'Utilitarianism',
    'Egalitarianism', 'Libertarianism', 'Authoritarianism', 'Totalitarianism'
]

fresh2 = [c.strip() for c in cands2 if c.strip().lower() not in existing]
print(f"Cand2 total: {len(cands2)}, Fresh2: {len(fresh2)}")
with open('fresh_pool2.json', 'w') as out:
    json.dump(fresh2, out, indent=2)
