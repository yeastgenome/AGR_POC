from .disease_ext import get_disease_record
from .primary_data_object_type import PrimaryDataObjectType
from loaders.transactions import Transaction


class DiseaseAlleleExt(object):

    def get_allele_disease_data(self, disease_data, batch_size, graph):
        list_to_yield = []
        dateProduced = disease_data['metaData']['dateProduced']
        dataProvider = disease_data['metaData']['dataProvider']
        release = None

        if 'release' in disease_data['metaData']:
            release = disease_data['metaData']['release']

        for diseaseRecord in disease_data['data']:

            diseaseObjectType = diseaseRecord['objectRelation'].get("objectType")

            if diseaseObjectType != PrimaryDataObjectType.allele.name:
                continue
            else:
                query = "match (g:Gene)-[]-(f:Feature) where f.primaryKey = {parameter} return g.primaryKey"
                featurePrimaryId = diseaseRecord.get('objectId')
                tx = Transaction(graph)
                returnSet = tx.run_single_parameter_query(query, featurePrimaryId)
                counter = 0
                allelicGeneId = ''
                for gene in returnSet:
                    counter += 1
                    allelicGeneId = gene["g.primaryKey"]
                if counter > 1:
                    allelicGeneId = ''
                    print ("returning more than one gene: this is an error")

                disease_features = get_disease_record(diseaseRecord, dateProduced, dataProvider, release, allelicGeneId)

                list_to_yield.append(disease_features)
                if len(list_to_yield) == batch_size:
                    yield list_to_yield

                    list_to_yield[:] = []  # Empty the list.

        if len(list_to_yield) > 0:
            yield list_to_yield
