package be.cytomine

import be.cytomine.ontology.Term
import be.cytomine.processing.Job
import be.cytomine.project.Project
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.HttpClient
import be.cytomine.test.Infos

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class StatsTests  {

    private void doGET(String URL, int expect) {
        HttpClient client = new HttpClient();
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        assert expect==code
    }

    void testRetrievalAVG() {
        Job job = BasicInstanceBuilder.createJobWithAlgoAnnotationTerm()
        String URL = Infos.CYTOMINEURL + "/api/stats/retrieval/avg.json?job=${job.id}"
        doGET(URL,200)

        URL = Infos.CYTOMINEURL + "/api/stats/retrieval/avg.json?job=-99"
        doGET(URL,404)
    }

    void testRetrievalAVGNotExist() {
        Job job = BasicInstanceBuilder.createJobWithAlgoAnnotationTerm()
        String URL = Infos.CYTOMINEURL + "/api/stats/retrieval/avg.json?job=${job.id}"
        doGET(URL,200)

        URL = Infos.CYTOMINEURL + "/api/stats/retrieval/avg.json?job=-99"
                doGET(URL,404)
    }


    void testRetrievalConfusionMatrix() {
        Job job = BasicInstanceBuilder.createJobWithAlgoAnnotationTerm()
        String URL = Infos.CYTOMINEURL + "/api/stats/retrieval/confusionmatrix.json?job=${job.id}"
        doGET(URL,200)

        URL = Infos.CYTOMINEURL + "/api/stats/retrieval/confusionmatrix.json?job=-99"
                doGET(URL,404)
    }

    void testRetrievalWorstTerm() {
        Job job = BasicInstanceBuilder.createJobWithAlgoAnnotationTerm()
        String URL = Infos.CYTOMINEURL + "/api/stats/retrieval/worstTerm.json?job=${job.id}"
        doGET(URL,200)
        URL = Infos.CYTOMINEURL + "/api/stats/retrieval/worstTerm.json?job=-99"
                doGET(URL,404)
    }

    void testRetrievalWorstAnnotation() {
        Job job = BasicInstanceBuilder.createJobWithAlgoAnnotationTerm()
        String URL = Infos.CYTOMINEURL + "/api/stats/retrieval/worstAnnotation.json?job=${job.id}"
        doGET(URL,200)
        URL = Infos.CYTOMINEURL + "/api/stats/retrieval/worstAnnotation.json?job=${job.id}"
                doGET(URL,200)
    }

    void testRetrievalWorstTermWithSuggest() {
        Job job = BasicInstanceBuilder.createJobWithAlgoAnnotationTerm()
        String URL = Infos.CYTOMINEURL + "/api/stats/retrieval/worstTermWithSuggest.json?job=${job.id}"
        doGET(URL,200)
        URL = Infos.CYTOMINEURL + "/api/stats/retrieval/worstTermWithSuggest.json?job=-99"
                doGET(URL,404)
    }

    void testRetrievalEvolution() {
        Job job = BasicInstanceBuilder.createJobWithAlgoAnnotationTerm()
        String URL = Infos.CYTOMINEURL + "/api/stats/retrieval/evolution.json?job=${job.id}"
        doGET(URL,200)
        URL = Infos.CYTOMINEURL + "/api/stats/retrieval/evolution.json?job=-99"
                doGET(URL,404)
    }

    void testRetrievalEvolutionAlgo() {
        Job job = BasicInstanceBuilder.createJobWithAlgoAnnotationTerm()
        String URL = Infos.CYTOMINEURL + "/api/stats/retrieval-evolution/evolution.json?job=${job.id}"
        doGET(URL,200)
        URL = Infos.CYTOMINEURL + "/api/stats/retrieval-evolution/evolution.json?job=-99"
                doGET(URL,404)
    }

    void testRetrievalEvolutionAlgoForTerm() {
        Job job = BasicInstanceBuilder.createJobWithAlgoAnnotationTerm()
        String URL = Infos.CYTOMINEURL + "/api/stats/retrieval-evolution/evolutionByTerm.json?job=${job.id}&term=${Term.findByOntology(job.project.ontology).id}"
        doGET(URL,200)
        URL = Infos.CYTOMINEURL + "/api/stats/retrieval-evolution/evolutionByTerm.json?job=-99&term=${Term.findByOntology(job.project.ontology).id}"
         doGET(URL,404)
    }



    void testStatTerm() {
        Project project = BasicInstanceBuilder.getProject()
        String URL = Infos.CYTOMINEURL + "/api/project/${project.id}/stats/term.json"
        doGET(URL,200)
        URL = Infos.CYTOMINEURL + "/api/project/-99/stats/term.json"
         doGET(URL,404)
    }

    void testStatUser() {
        Project project = BasicInstanceBuilder.getProject()
        String URL = Infos.CYTOMINEURL + "/api/project/${project.id}/stats/user.json"
        doGET(URL,200)
        URL = Infos.CYTOMINEURL + "/api/project/-99/stats/user.json"
         doGET(URL,404)
    }


    void testStatsTermslide() {
        Project project = BasicInstanceBuilder.getProject()
        String URL = Infos.CYTOMINEURL + "/api/project/${project.id}/stats/termslide.json"
        doGET(URL,200)
        URL = Infos.CYTOMINEURL + "/api/project/-99/stats/termslide.json"
         doGET(URL,404)
    }

    void testStatsUserAnnotation() {
        Project project = BasicInstanceBuilder.getProject()
        String URL = Infos.CYTOMINEURL + "/api/project/${project.id}/stats/userannotations.json"
        doGET(URL,200)
        URL = Infos.CYTOMINEURL + "/api/project/-99/stats/userannotations.json"
        doGET(URL,404)
    }

    void testStatsUserSlide() {
        Project project = BasicInstanceBuilder.getProject()
        String URL = Infos.CYTOMINEURL + "/api/project/${project.id}/stats/userslide.json"
        doGET(URL,200)
        URL = Infos.CYTOMINEURL + "/api/project/-99/stats/userslide.json"
        doGET(URL,404)
    }

    void testStatsEvolution() {
        Project project = BasicInstanceBuilder.getProject()
        String URL = Infos.CYTOMINEURL + "/api/project/${project.id}/stats/annotationevolution.json"
        doGET(URL,200)
        URL = Infos.CYTOMINEURL + "/api/project/${project.id}/stats/annotationevolution.json?term=${project.ontology.leafTerms().first()}"
        doGET(URL,200)
        URL = Infos.CYTOMINEURL + "/api/project/-99/stats/annotationevolution.json"
        doGET(URL,404)
    }

}
