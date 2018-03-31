from .mod import MOD

class AGR_MOD(MOD):

    def __init__(self):
        self.species = "Candida albicans"
        self.loadFile = "CGD_1.0.0.0.tar.gz"
        self.bgiName = "/CGD_1.0.0.0/CGD_1.0.0.0_basicGeneInformation.json"
        self.diseaseName = "/CGD_1.0.0.0/disease_association_CGD.1.0.0.json"
        self.alleleName = "/CGD_1.0.0.0_allele.json"
        self.geneAssociationFile = "gene_association_1.0.0.0.cgd.gz"
        self.identifierPrefix = "CGD:"

    def load_genes(self, batch_size, testObject):
        data = MOD.load_genes_mod(self, batch_size, testObject, self.bgiName, self.loadFile)
        return data

    @staticmethod
    def gene_href(gene_id):
        return "http://www.candidagenome.org/locus/" + gene_id + "/overview"

    @staticmethod
    def get_organism_names():
        return ["Candida albicans", "C. albicans", "YEAST"]

    def extract_go_annots(self, testObject):
        go_annot_list = MOD.extract_go_annots_mod(
            self, self.geneAssociationFile, self.species, self.identifierPrefix,
            testObject)
        return go_annot_list

    def load_disease_gene_objects(self, batch_size, testObject):
        data = MOD.load_disease_gene_objects_mod(
            self, batch_size, testObject, self.diseaseName, self.loadFile)
        return data

# these are commented out because SGD has no allele data and no allele->disease data right now

    def load_disease_allele_objects(self, batch_size, testObject, graph):
        data = ""
        #MOD.load_disease_allele_objects_mod(batch_size, testObject, SGD.diseaseName, SGD.loadFile, graph)
        return data

    def load_allele_objects(self, batch_size, testObject):
        data = ""
        #MOD.load_allele_objects_mod(self, batch_size, testObject, self.alleleName, self.loadFile)
        return data
