package edu.cmu.geoparser.nlp.spelling;

import java.io.*;
import java.util.ArrayList;

import edu.cmu.geoparser.common.StringUtil;
import edu.cmu.geoparser.io.GetReader;
import edu.cmu.geoparser.io.GetWriter;
import edu.cmu.geoparser.nlp.soundex.Soundex;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

public class LearningToRankDictionaryMatching {

	public static  int FEATURE_COUNT;
	public static svm_parameter p;

	LearningToRankDictionaryMatching() {
		p = new svm_parameter();
		p.svm_type = svm_parameter.C_SVC;
		p.kernel_type = svm_parameter.RBF;
		p.degree = 3;
		p.gamma = 0.5; // 1/k
		p.coef0 = 0;
		p.nu = 0.5;
		p.cache_size = 40;
		p.C = 1;
		p.eps = 1e-3;
		p.p = 0.1;
		p.shrinking = 1;
		p.nr_weight = 0;
		p.weight_label = new int[0];
		p.weight = new double[3];
		p.probability = 0;
	}

	public static void main(String argv[]) throws IOException, InterruptedException {
		argv[0] = "gazetteerMatchingInput/precision-10-matches.csv";
		argv[1] = "gazetteerMatchingInput/SVM-precision-10-matches.csv";
		argv[2] = "./modelTest.mdl";
		LearningToRankDictionaryMatching ltr = new LearningToRankDictionaryMatching();
		ltr.TrainReRanking(argv);
		Thread.sleep(1000);
		ltr.Test_Re_ranking(argv);
	}

	public void Test_Re_ranking(String argv[])throws IOException{
		
		LearningToRankDictionaryMatching ltr = new LearningToRankDictionaryMatching();
//		DictionaryMerging mc = new DictionaryMerging(GetReader.getIndexSearcher("GazIndex"));
		BufferedReader br = GetReader.getUTF8FileReader(argv[0]);
		BufferedWriter bw = GetWriter.getFileWriter(argv[1]);
		
		svm_model model = svm.svm_load_model(argv[2]);
		int line = 0;
		ArrayList<Double> x = new ArrayList<Double>();
		int accuracy = 0;
		int dmatch = 0,dcont=0,dunmatch=0,m=0,c=0,u=0;
		int lmatch =0, lcont=0, lunmatch=0;
		// Read user input
		String phrase = br.readLine();
		while ((phrase = br.readLine())!=null) {
			line++;
			//System.out.println(phrase);
			String[] toks = phrase.split("	");
			int label = 0;
			if ( toks.length==10)
				if (toks[9].length()!=0)
					label = Integer.parseInt(toks[9]);
			String query = StringUtil.getDeAccentLoweredString(toks[0].trim());
			double dlat = Double.parseDouble(toks[1]);
			double dlon = Double.parseDouble(toks[2]);
			
			String candidate = StringUtil.getDeAccentLoweredString(toks[4].trim());
			double clat = Double.parseDouble(toks[5]);
			double clon = Double.parseDouble(toks[6]);
			
			x = ltr.generateFeature(query,dlat,dlon,candidate,clat,clon);
			svm_node[] instance = new svm_node[FEATURE_COUNT];
			for (int j = 0; j < instance.length; j ++){
				instance[j] = new svm_node();
				instance[j].index = j;
				instance[j].value = x.get(j);
			}
			double[] probability = new double[10];
			int l = (int)svm.svm_predict(model, instance);//(model, instance, probability);
			bw.write(phrase +"	"+l+"\n");
			
			if (label ==2) dmatch++;
			else if (label ==1)dcont++;
			else dunmatch++;
			
			if (l==2) lmatch++;
			else if(l==1) lcont++;
			else lunmatch++;
			
			if (l==label)
			{	if (l==2) m++;
				else if (l==1) c++;
				else u++;
			}
		
		}
		System.out.println("match precision:" +(double)m/(double)lmatch);
		System.out.println("match recall:" +(double)m/(double)dmatch);
		System.out.println("match f1:" +(double)(2*m)/(double)(dmatch+lmatch));
		System.out.println(dmatch);
		System.out.println(lmatch);
		System.out.println("containment precision:" +(double)c/(double)lcont);
		System.out.println("containment recall:" +(double)c/(double)dcont);
		System.out.println("containment f1:" +(double)(2*c)/(double)(dcont+lcont));
		System.out.println(dcont);
		System.out.println(lcont);
		System.out.println("unmatch precision:" +(double)u/(double)lunmatch);
		System.out.println("unmatch recall:" +(double)u/(double)dunmatch);
		System.out.println("unmatch f1:" +(double)(2*u)/(double)(dunmatch+lunmatch));
		System.out.println(dunmatch);
		System.out.println(lunmatch);
		System.out.println("overall Precision: "+ (double)(u+m+c)/(double)(lmatch+lcont+lunmatch));
		System.out.println("overall Recall: "+ (double)(u+m+c)/(double)line);
		System.out.println("overall f1: "+ (double)(2*(u+m+c))/(double)(line+lmatch+lcont+lunmatch));
		bw.close();
	}

	public void TrainReRanking(String argv[]) throws IOException {

		BufferedReader br = GetReader.getUTF8FileReader(argv[0]);

		int line = 0;
		ArrayList<ArrayList<Double>> x = new ArrayList<ArrayList<Double>>(2000);
		ArrayList<Double> y = new ArrayList<Double>();

		// Read user input
		String phrase = br.readLine();
		while ((phrase = br.readLine()) != null) {
			line++;
			// System.out.println(phrase);
			String[] toks = phrase.split("	");

			String query = StringUtil.getDeAccentLoweredString(toks[0].trim());
			double dlat = Double.parseDouble(toks[1]);
			double dlon = Double.parseDouble(toks[2]);

			String candidate = StringUtil.getDeAccentLoweredString(toks[4].trim());
			double clat = Double.parseDouble(toks[5]);
			double clon = Double.parseDouble(toks[6]);
			double label = 0;
			if (toks.length==10)					
			{
				if (toks[9].length()!=0)
					label = Double.parseDouble(toks[9]);
			}
			String wquery = query.replaceAll("\\s", "");
			String wcandidate = candidate.replaceAll("\\s", "");

			ArrayList<Double> vector = generateFeature(query, dlat, dlon, candidate, clat, clon);
			FEATURE_COUNT=vector.size();
			x.add(vector);
			y.add(label);
		}
		svm_problem problem = new svm_problem();

		problem.l = line;
		problem.y = new double[problem.l];
		problem.x = new svm_node[problem.l][];

		for (int j = 0; j < problem.l; j++) {
			problem.y[j] = y.get(j);
			problem.x[j] = new svm_node[FEATURE_COUNT];
			for (int i = 0; i < FEATURE_COUNT; i++) {
				problem.x[j][i] = new svm_node();
				problem.x[j][i].index = i;
				problem.x[j][i].value = x.get(j).get(i);
				// System.out.print(" "+problem.x[j][i]);
			}
		}

		// todo: put the x and y here
		svm_model model = svm.svm_train(problem, p);
		svm.svm_save_model("./modelTest.mdl", model);
//		double[] prob = new double[10000];
//		svm.svm_cross_validation(problem, p, 5, prob);
//		for ( double i : prob) System.out.println(i);

	}

	private ArrayList<Double> generateFeature(String query, double dlat, double dlon, String candidate, double clat,
			double clon) {
		// TODO Auto-generated method stub
		String[] q_bigram = StringUtil.getBigram(query.toCharArray());
		String[] q_trigram = StringUtil.getTrigram(query.toCharArray());
		String[] c_bigram = StringUtil.getBigram(candidate.toCharArray());
		String[] c_trigram = StringUtil.getTrigram(candidate.toCharArray());
		// f1: word level similarity score
		double s_wsim
		//=0;
		= StringUtil.getGramSimilarity(query.split(" "), candidate.split(" "));

		// f2: editing distance
		double s_ed
		// =0;
		= StringUtil.editDistance(query.replace(" ",""), candidate.replace(" ",""));

		// f3: bigram
		double s_bigram
		//=0;
		= StringUtil.getGramSimilarity(q_bigram, c_bigram);
		// f4: trigram
		double s_trigram
		// =0;
		= StringUtil.getGramSimilarity(q_trigram, c_trigram);

		// f5: head matching
		double s_head
		// =0;
		= 1.0d - (double) StringUtil.commonLengthfromHead(query, candidate) / (double) query.length();

		// f6: average head matching
		double s_aveHead
		// =0;
		= StringUtil.PhraseCommonHeadRatio(query, candidate);
		// System.out.println( s_aveHead);

		// f7: distance
		double dist
		// =0;
		= 1.0d - Math.sqrt(Math.pow(clat - dlat, 2) + Math.pow(clon - dlon, 2)) * Math.sqrt(2);

		// f8: containment
		double cont 
		// =0;
		= (query.contains(candidate) || candidate.contains(query)) ? 1: 0;
		
		// f9: sondex match
		double soundex 
		//=0;
		= Soundex.soundex(query).equals(Soundex.soundex(candidate))?1:0;
		
		ArrayList<Double> vector = new ArrayList<Double>(FEATURE_COUNT);

		vector.add(s_wsim);
		vector.add(s_ed);// F7
		vector.add(s_bigram);
		vector.add(s_trigram);
		vector.add(s_head); //F4
		vector.add(s_aveHead); // F5
		vector.add(dist); //F6
		vector.add(cont); // F8
		vector.add(soundex); //F9
		return vector;
	}
}
