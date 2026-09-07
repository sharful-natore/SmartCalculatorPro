import json

with open('app/src/main/assets/dictionary_1000.json') as f:
    d = json.load(f)
existing = set(w['word'].strip().lower() for w in d)

candidates_pool = [
    # Medicine & Biology
    'Homeostasis', 'Vasodilation', 'Vasoconstriction', 'Ischemia', 'Hypoxia', 'Anoxia', 'Edema',
    'Aneurysm', 'Thrombosis', 'Embolism', 'Hematoma', 'Paresthesia', 'Proprioception', 'Nociception',
    'Apoptosis', 'Necrosis', 'Phagocytosis', 'Cytokinesis', 'Mitosis', 'Synapse', 'Neurotransmitter',
    'Myelin', 'Axon', 'Dendrite', 'Glial', 'Pathogen', 'Antigen', 'Antibody', 'Epitope', 'Leukocyte',
    'Erythrocyte', 'Thrombocyte', 'Platelet', 'Hemoglobin', 'Myoglobin', 'Peristalsis', 'Enzyme',
    'Substrate', 'Catalyst', 'Endocrine', 'Exocrine', 'Hormone', 'Receptor', 'Ligand', 'Gastrulation',
    'Blastula', 'Zygote', 'Gamete', 'Haploid', 'Diploid', 'Genotype', 'Phenotype', 'Allele',
    
    # Architecture & Structural Elements
    'Pergola', 'Minaret', 'Cantilever', 'Pediment', 'Pilaster', 'Caryatid', 'Entablature', 'Soffit',
    'Lintel', 'Transom', 'Spandrel', 'Keystone', 'Oculus', 'Cupola', 'Flying buttress', 'Campanile',
    'Gargoyle', 'Grotesque', 'Stanchion', 'Bollard', 'Baluster', 'Colonnade', 'Peristyle', 'Atrium',
    'Hypostyle', 'Pylon', 'Obelisk', 'Ziggurat', 'Stupa', 'Pagoda', 'Amphitheater', 'Narthex',
    
    # Earth Sciences, Geology & Geography
    'Permafrost', 'Watershed', 'Regolith', 'Scree', 'Moraine', 'Drumlin', 'Esker', 'Cirque',
    'Arete', 'Caldera', 'Fumarole', 'Solfatara', 'Cenote', 'Atoll', 'Guyot', 'Mesa', 'Butte',
    'Oxbow lake', 'Alluvial fan', 'Playa', 'Sinkhole', 'Karst', 'Stalactite', 'Stalagmite',
    'Speleothem', 'Basalt', 'Granite', 'Obsidian', 'Pumice', 'Rhyolite', 'Andesite', 'Schist',
    'Gneiss', 'Quartzite', 'Sedimentary', 'Metamorphic', 'Igneous', 'Stratum', 'Fossiliferous',
    
    # Astronomy & Space
    'Perihelion', 'Aphelion', 'Perigee', 'Apogee', 'Syzygy', 'Penumbra', 'Umbra', 'Antumbra',
    'Parallax', 'Albedo', 'Magnetosphere', 'Heliopause', 'Chromosphere', 'Photosphere', 'Corona',
    'Supernova', 'Pulsar', 'Quasar', 'Magnetar', 'Exoplanet', 'Accretion disk', 'Event horizon',
    'Singularity', 'Nebula', 'Planetesimal', 'Oort cloud', 'Kuiper belt', 'Asteroid', 'Meteoroid',
    
    # Linguistics, Writing & Phonology
    'Diacritic', 'Umlaut', 'Tilde', 'Cedilla', 'Circumflex', 'Macron', 'Breve', 'Digraph',
    'Diphthong', 'Trigraph', 'Ligature', 'Syllabary', 'Ideogram', 'Pictogram', 'Logogram',
    'Phoneme', 'Morpheme', 'Allophone', 'Allomorph', 'Grapheme', 'Lexeme', 'Semantics', 'Pragmatics',
    'Orthography', 'Etymology', 'Cognate', 'Loanword', 'Calque', 'Neologism', 'Archaism', 'Portmanteau',
    
    # Economics & Commerce
    'Seigniorage', 'Arbitrage', 'Contango', 'Backwardation', 'Monopsony', 'Oligopsony', 'Duopoly',
    'Stagflation', 'Deflation', 'Hyperinflation', 'Liquidity', 'Solvency', 'Amortization', 'Annuity',
    'Depreciation', 'Appreciation', 'Fiscal policy', 'Monetary policy', 'Tariff', 'Subsidy', 'Embargo',
    'Quotas', 'Cartel', 'Conglomerate', 'Monopoly', 'Oligopoly', 'Underwriting', 'Collateral',
    
    # Literature, Poetry & Rhetoric
    'Ottava rima', 'Terza rima', 'Spenserian', 'Caesura', 'Enjambment', 'Spondee', 'Pyrrhic',
    'Dactyl', 'Anapest', 'Trochee', 'Scansion', 'Hemistich', 'Villanelle', 'Sestina', 'Rondeau',
    'Triolet', 'Ballade', 'Blank verse', 'Free verse', 'Alexandrine', 'Heroic couplet', 'Iambic',
    'Canto', 'Stanza', 'Refrain', 'Alliteration', 'Assonance', 'Consonance', 'Onomatopoeia',
    
    # Law & Governance
    'Amicus curiae', 'Injunction', 'Indemnity', 'Tort', 'Estoppel', 'Usufruct', 'Subrogation',
    'Jurisprudence', 'Mens rea', 'Actus reus', 'Corpus delicti', 'Nolo contendere', 'Prima facie',
    'Pro bono', 'Ex post facto', 'De jure', 'De facto', 'Ultra vires', 'Interim', 'Affidavit',
    
    # Philosophy & Ethics
    'Consequentialism', 'Utilitarianism', 'Deontology', 'Intuitionism', 'Emotivism', 'Prescriptivism',
    'Eudaimonism', 'Hedonism', 'Asceticism', 'Stoicism', 'Epicureanism', 'Cynicism', 'Skepticism',
    'Solipsism', 'Quietism', 'Determinism', 'Fatalism', 'Existentialism', 'Nihilism', 'Absurdism'
]

unseen = [w for w in candidates_pool if w.lower() not in existing]
seen = [w for w in candidates_pool if w.lower() in existing]

print(f"Total candidates tested: {len(candidates_pool)}")
print(f"Unseen available: {len(unseen)}")
print(f"Already in dict: {len(seen)}")
