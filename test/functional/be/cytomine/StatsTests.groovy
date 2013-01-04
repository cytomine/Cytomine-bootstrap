package be.cytomine

import be.cytomine.processing.Job
import be.cytomine.test.BasicInstance
import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON
import be.cytomine.ontology.Term

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class StatsTests extends functionaltestplugin.FunctionalTestCase {

    private void doGET(String URL, int expect) {
        HttpClient client = new HttpClient();
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        assertEquals(expect,code)
    }

    void testRetrievalAVG() {
        Job job = BasicInstance.createJobWithAlgoAnnotationTerm()
        String URL = Infos.CYTOMINEURL + "/api/stats/retrieval/avg.json?job=${job.id}"
        doGET(URL,200)

        URL = Infos.CYTOMINEURL + "/api/stats/retrieval/avg.json?job=-99"
        doGET(URL,404)
    }

    void testRetrievalAVGNotExist() {
        Job job = BasicInstance.createJobWithAlgoAnnotationTerm()
        String URL = Infos.CYTOMINEURL + "/api/stats/retrieval/avg.json?job=${job.id}"
        doGET(URL,200)

        URL = Infos.CYTOMINEURL + "/api/stats/retrieval/avg.json?job=-99"
                doGET(URL,404)
    }


    void testRetrievalConfusionMatrix() {
        Job job = BasicInstance.createJobWithAlgoAnnotationTerm()
        String URL = Infos.CYTOMINEURL + "/api/stats/retrieval/confusionmatrix.json?job=${job.id}"
        doGET(URL,200)

        URL = Infos.CYTOMINEURL + "/api/stats/retrieval/confusionmatrix.json?job=-99"
                doGET(URL,404)
    }

    void testRetrievalWorstTerm() {
        Job job = BasicInstance.createJobWithAlgoAnnotationTerm()
        String URL = Infos.CYTOMINEURL + "/api/stats/retrieval/worstTerm.json?job=${job.id}"
        doGET(URL,200)
        URL = Infos.CYTOMINEURL + "/api/stats/retrieval/worstTerm.json?job=-99"
                doGET(URL,404)
    }

    void testRetrievalWorstAnnotation() {
        Job job = BasicInstance.createJobWithAlgoAnnotationTerm()
        String URL = Infos.CYTOMINEURL + "/api/stats/retrieval/worstAnnotation.json?job=${job.id}"
        doGET(URL,200)
        URL = Infos.CYTOMINEURL + "/api/stats/retrieval/worstAnnotation.json?job=${job.id}"
                doGET(URL,200)
    }

    void testRetrievalWorstTermWithSuggest() {
        Job job = BasicInstance.createJobWithAlgoAnnotationTerm()
        String URL = Infos.CYTOMINEURL + "/api/stats/retrieval/worstTermWithSuggest.json?job=${job.id}"
        doGET(URL,200)
        URL = Infos.CYTOMINEURL + "/api/stats/retrieval/worstTermWithSuggest.json?job=-99"
                doGET(URL,404)
    }

    void testRetrievalEvolution() {
        Job job = BasicInstance.createJobWithAlgoAnnotationTerm()
        String URL = Infos.CYTOMINEURL + "/api/stats/retrieval/evolution.json?job=${job.id}"
        doGET(URL,200)
        URL = Infos.CYTOMINEURL + "/api/stats/retrieval/evolution.json?job=-99"
                doGET(URL,404)
    }

    void testRetrievalEvolutionAlgo() {
        Job job = BasicInstance.createJobWithAlgoAnnotationTerm()
        String URL = Infos.CYTOMINEURL + "/api/stats/retrieval-evolution/evolution?job=${job.id}"
        doGET(URL,200)
        URL = Infos.CYTOMINEURL + "/api/stats/retrieval-evolution/evolution?job=-99"
                doGET(URL,404)
    }

    void testRetrievalEvolutionAlgoForTerm() {
        Job job = BasicInstance.createJobWithAlgoAnnotationTerm()
        String URL = Infos.CYTOMINEURL + "/api/stats/retrieval-evolution/evolutionByTerm?job=${job.id}&term=${Term.findByOntology(job.project.ontology).id}"
        doGET(URL,200)
        URL = Infos.CYTOMINEURL + "/api/stats/retrieval-evolution/evolutionByTerm?job=-99&term=${Term.findByOntology(job.project.ontology).id}"
         doGET(URL,404)
    }
}
