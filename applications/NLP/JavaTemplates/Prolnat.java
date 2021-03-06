import java.io.File;
import java.io.IOException;


import java.net.URI;
import java.util.ArrayList;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.streaming.StreamInputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.hadoop.io.NullWritable;


//import com.javainc.jperl.JPerl;
//import com.javainc.jperl.ParseException;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

@SuppressWarnings("deprecation")
public class Prolnat extends Configured implements Tool {

	private static final Log LOG = LogFactory.getLog(Prolnat.class);

	public static void main(String[] args) throws Exception {



		int res = ToolRunner.run(new Configuration(), new Prolnat(), args);
		System.exit(res);

	}

	@Override
	public int run(String[] args) throws Exception {
		//Configuration conf = new Configuration();
		Configuration conf = this.getConf();

		//String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();

		if (args.length < 2) {
			System.err.println("Usage: hadoop jar Prolnat.jar <in> <out> ");
			System.exit(2);
		}


		//Ponhemos o timeout indefinido, non sabemos canto pode tardar
		conf.set("mapreduce.task.timeout", "0");
		conf.set("xmlinput.start","<doc>");
		conf.set("xmlinput.end", "</doc>");

		//conf.set("mapreduce.input.lineinputformat.linespermap","100");
		Job job = new Job(conf,"Prolnat");

		//String hostHDFS = conf.get("fs.default.name");

		//job.addCacheArchive(new URI(hostHDFS+"/PLN/Prolnat.zip"));


		job.setJarByClass(Prolnat.class);
		job.setMapperClass(PLNMapper.class);
		//job.setCombinerClass(PLNReducer.class);
		//job.setReducerClass(PLNReducer.class);
		//job.setInputFormatClass(MultiLineInputFormat.class);
		job.setNumReduceTasks(0);


		//INPUT FORMAT
		//job.setInputFormatClass(XmlInputFormat.class);
		//job.setInputFormatClass(WholeFileInputFormat.class);
		//job.setInputFormatClass(NonSplittableTextInputFormat.class);

		//job.setMapOutputKeyClass(Text.class);
		//job.setMapOutputValueClass(String.class);

		//job.setOutputKeyClass(Text.class);
		job.setOutputKeyClass(NullWritable.class);
		job.setOutputValueClass(Text.class);

		//job.setOutputKeyClass(IntWritable.class);
		//job.setOutputValueClass(String.class);


		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));


		return(job.waitForCompletion(true) ? 0 : 1);

	}


	//public static class PLNMapper extends Mapper<Object, Text, Text, Text>{
	public static class PLNMapper extends Mapper<Object, Text,NullWritable , Text> {

		private Text word = new Text();

		private String rutaPLNLocal = "";
		private String resultado = "";

		Sentences modSentences = new Sentences();
		Tokens modTokens = new Tokens();
		Splitter modSplitter = new Splitter();
		NER modNer = new NER();
		Tagger modTagger = new Tagger();
		NEC modNec = new NEC();

		//public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
		@Override
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			try{

				int i = 0;

				ArrayList<String> sentences;// = new ArrayList<String>();
				ArrayList<String> tokens; // = new ArrayList<String>();
				ArrayList<String> splits; // = new ArrayList<String>();
				ArrayList<String> ner; // = new ArrayList<String>();
				ArrayList<String> tagger;
				ArrayList<String> nec;
				
				
					
				//System.out.println("Entrada do mapper :: "+value.toString());
						    	
				
				for(String entrada: value.toString().split("\n")){

					/*sentences.clear();
					tokens.clear();
					splits.clear();
					ner.clear();
					tagger.clear();
					nec.clear();
					*/
									    	
					
					sentences = new ArrayList<String>();
					tokens = new ArrayList<String>();
					splits = new ArrayList<String>();
					ner = new ArrayList<String>();
					tagger = new ArrayList<String>();
					nec = new ArrayList<String>();
					
					//System.out.println("ENTRADA: "+entrada);
					//context.getConfiguration().get(arg0)
					
					sentences = modSentences.runSentencesModule(entrada);
					//sentences = modSentences.runSentencesModule(value.toString());
					
					//LOG.info("Sentences rematado");
					/*for(i = 0; i<sentences.size();i++){
				    	System.out.println("Sentence procesada: "+sentences.get(i));
				    }*/

					tokens = modTokens.runTokens(sentences);
					//LOG.info("Tokens rematado");
					/*for(i = 0; i<tokens.size();i++){
				    	System.out.print(tokens.get(i));
				    }*/

					sentences.clear();

					splits = modSplitter.runSplitter(tokens);

					tokens.clear();
					
					ner = modNer.runNer(splits);
					
					splits.clear();
					
					tagger = modTagger.runTagger(ner);
					
					ner.clear();
					
					nec = modNec.nec_es(tagger);
					
					tagger.clear();
					
					for(i = 0; i<nec.size();i++){
						context.write(NullWritable.get(), new Text(nec.get(i)));
					}
					
					nec.clear();
					

				}
			}
			catch(Exception e){
				System.out.println(e.toString());
			}
		}

	}





}
