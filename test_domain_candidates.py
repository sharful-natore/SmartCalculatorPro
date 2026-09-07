import json

with open('app/src/main/assets/dictionary_1000.json') as f:
    existing = set(w['word'].strip().lower() for w in json.load(f))

# Candidate pools:
# Domain 1: Linguistics, Writing Systems, Rhetoric & Poetics (target: 100 words)
ling_candidates = [
    'Diacritic', 'Umlaut', 'Cedilla', 'Circumflex', 'Macron', 'Breve', 'Digraph', 'Diphthong',
    'Trigraph', 'Ligature', 'Syllabary', 'Ideogram', 'Pictogram', 'Logogram', 'Phoneme', 'Morpheme',
    'Allophone', 'Allomorph', 'Grapheme', 'Lexeme', 'Semantics', 'Pragmatics', 'Orthography',
    'Etymology', 'Cognate', 'Loanword', 'Calque', 'Portmanteau', 'Ottava rima', 'Terza rima',
    'Spenserian', 'Caesura', 'Enjambment', 'Spondee', 'Pyrrhic', 'Dactyl', 'Anapest', 'Trochee',
    'Scansion', 'Hemistich', 'Villanelle', 'Rondeau', 'Triolet', 'Ballade', 'Alexandrine',
    'Heroic couplet', 'Canto', 'Refrain', 'Assonance', 'Consonance', 'Onomatopoeia', 'Apostrophe',
    'Euphemism', 'Hyperbole', 'Litotes', 'Metonymy', 'Oxymoron', 'Paradox', 'Personification',
    'Pun', 'Simile', 'Understatement', 'Alliteration', 'Bathos', 'Pathos', 'Ethos', 'Logos',
    'Catharsis', 'Hubris', 'Hamartia', 'Nemesis', 'Peripeteia', 'Anagnorisis', 'Deus ex machina',
    'Soliloquy', 'Aside', 'Monologue', 'Dialogue', 'Prologue', 'Epilogue', 'Chorale', 'Strophe',
    'Antistrophe', 'Epode', 'In media res', 'Foreshadowing', 'Flashback', 'Motif', 'Archetype',
    'Allegory', 'Parable', 'Fable', 'Apotheosis', 'Bildungsroman', 'Epistolary', 'Picaresque',
    'Gothic', 'Stream of consciousness', 'Unreliable narrator', 'Dramatic irony', 'Verisimilitude'
]

# Domain 2: Economics, Finance, Banking & Law (target: 100 words)
econ_law_candidates = [
    'Seigniorage', 'Arbitrage', 'Contango', 'Backwardation', 'Monopsony', 'Oligopsony', 'Duopoly',
    'Stagflation', 'Hyperinflation', 'Solvency', 'Amortization', 'Annuity', 'Fiscal policy',
    'Monetary policy', 'Embargo', 'Underwriting', 'Collateral', 'Amicus curiae', 'Injunction',
    'Indemnity', 'Tort', 'Estoppel', 'Usufruct', 'Subrogation', 'Mens rea', 'Actus reus',
    'Corpus delicti', 'Nolo contendere', 'Prima facie', 'Pro bono', 'Ex post facto', 'De jure',
    'De facto', 'Ultra vires', 'Interim', 'In re', 'Ipso facto', 'Modus operandi', 'Non sequitur',
    'Status quo', 'Sub judice', 'Sine qua non', 'Force majeure', 'Bona fides', 'Quantum meruit',
    'Ultra vires', 'Pari passu', 'Caveat emptor', 'Ex officio', 'Ad hoc', 'Inter alia',
    'Mutatis mutandis', 'Prima facie', 'Quid pro quo', 'Stare decisis', 'Vox populi', 'Bailment',
    'Chattel', 'Covenant', 'Damages', 'Defamation', 'Deposition', 'Embezzlement', 'Encumbrance',
    'Equity', 'Escheat', 'Escrow', 'Estoppel', 'Eviction', 'Exoneration', 'Extradition',
    'Felony', 'Fiduciary', 'Foreclosure', 'Franchise', 'Garnishment', 'Guarantor', 'Hearsay',
    'Indictment', 'Insolvency', 'Intestate', 'Judgment', 'Jurisdiction', 'Larceny', 'Leasehold',
    'Lien', 'Liquidated damages', 'Malfeasance', 'Misdemeanor', 'Mitigation', 'Negligence',
    'Notary', 'Nuisance', 'Perjury', 'Plaintiff', 'Plea bargain', 'Precedent', 'Probate'
]

# Domain 3: Philosophy, Ethics, Human Mind & GRE Vocabulary (target: 100 words)
phil_vocab_candidates = [
    'Consequentialism', 'Utilitarianism', 'Intuitionism', 'Emotivism', 'Prescriptivism',
    'Eudaimonism', 'Asceticism', 'Cynicism', 'Fatalism', 'Existentialism', 'Absurdism',
    'Solipsism', 'Nihilism', 'Determinism', 'Altruism', 'Egoism', 'Relativism', 'Absolutism',
    'Deconstructivism', 'Structuralism', 'Postmodernism', 'Modernism', 'Humanism', 'Empiricism',
    'Rationalism', 'Behaviorism', 'Cognitivism', 'Gestalt', 'Psychoanalysis', 'Subconscious',
    'Archetype', 'Persona', 'Shadow self', 'Anima', 'Animus', 'Individuation', 'Catharsis',
    'Sublimation', 'Repression', 'Projection', 'Rationalization', 'Introjection', 'Displacement',
    'Regression', 'Cognitive dissonance', 'Confirmation bias', 'Halo effect', 'Placebo effect',
    'Nocebo effect', 'Self-efficacy', 'Neuroplasticity', 'Epigenetics', 'Symbiosis', 'Mutualism',
    'Commensalism', 'Parasitism', 'Bioluminescence', 'Photosynthesis', 'Cellular respiration',
    'Fermentation', 'Glycolysis', 'Krebs cycle', 'Oxidative phosphorylation', 'Endosymbiosis',
    'Speciation', 'Allopatric', 'Sympatric', 'Adaptive radiation', 'Convergent evolution',
    'Divergent evolution', 'Phylogeny', 'Cladistics', 'Taxonomy', 'Binomial nomenclature',
    'Endemic species', 'Keystone species', 'Indicator species', 'Invasive species', 'Trophic level',
    'Food web', 'Ecological niche', 'Succession', 'Climax community', 'Carrying capacity',
    'Exponential growth', 'Logistic growth', 'Biodiversity', 'Ecosystem service', 'Carbon footprint'
]

print("Ling candidates total:", len(ling_candidates), "unseen:", len([w for w in ling_candidates if w.lower() not in existing]))
print("Econ/Law candidates total:", len(econ_law_candidates), "unseen:", len([w for w in econ_law_candidates if w.lower() not in existing]))
print("Phil/Vocab candidates total:", len(phil_vocab_candidates), "unseen:", len([w for w in phil_vocab_candidates if w.lower() not in existing]))
