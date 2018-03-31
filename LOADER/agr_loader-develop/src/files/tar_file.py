import tarfile
import os

class TARFile(object):

    def __init__(self, path, tarfilename):
        self.path = path
        self.tarfilename = tarfilename

    def extract_all(self):
        print("Extracting files from %s/%s ..." % (self.path, self.tarfilename))

        tfile = tarfile.open(self.path + "/" + self.tarfilename, 'r')
        extract = False
        for member in tfile.getmembers():
            if not os.path.exists(self.path + "/" + member.name):
                print("Extracting (%s->%s/%s)" % (member.name, self.path, member.name))
                extract = True
        if extract is True:
            tfile.extractall(self.path)