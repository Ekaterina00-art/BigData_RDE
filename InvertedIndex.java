import javax.xml.soap.Text;
import java.io.IOException;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class InvertedIndex {
    public static class InverseIndexMapper extends Mapper<LongWritable, Text, Text, Text>{
        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            // TODO Auto-generated method stub
            String line = value.toString();
            String[] strTokens = line.split(" ");
            FileSplit inputSplit = (FileSplit)context.getInputSplit();
            Path path = inputSplit.getPath();
            String pathStr = path.toString();
            int index = pathStr.lastIndexOf("/");
            String strFileName = pathStr.substring(index + 1);
            for (String token : strTokens) {
                if (token != " " && token != "\t") {
                    context.write(new Text(token + "->" + strFileName), new Text("1"));
                }
            }
        }

        public static class InverseIndexCombiner extends Reducer<Text, Text, Text, Text> {
            static Map<String, Integer> map = new HashMap<String, Integer>();
            static String strWord = null;

            @Override
            protected void reduce(Text key, Iterable<Text> values, Context context)
                    throws IOException, InterruptedException {
                // TODO Auto-generated method stub
                String[] tokens = key.toString().split("->");
                if (strWord == null) {
                    strWord = tokens[0];
                }
                if (strWord.equals(tokens[0])) {
                    String strFileName = tokens[1];
                    int freq = 0;
                    for (Text value : values) {
                        freq += Integer.parseInt(value.toString());
                    }
                    map.put(strFileName, freq);
                } else {
                    String strNewValue = "";
                    double aveFreq = 0;
                    for (Map.Entry<String, Integer> entry : map.entrySet()) {
                        strNewValue += entry.getKey() + ":" + entry.getValue() + ",";
                        aveFreq += (double) entry.getValue();
                    }
                    aveFreq /= (double) map.size();
                    Text newKey = new Text(strWord);
                    map.clear();
                    context.write(newKey, new Text(strNewValue));
                    context.write(newKey, new Text("" + aveFreq));
                    strWord = tokens[0];
                    String strFileName = tokens[1];
                    int freq = 0;
                    for (Text value : values) {
                        freq += Integer.parseInt(value.toString());
                    }
                    map.put(strFileName, freq);
                }
            }
        }
        public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
            // TODO Auto-generated method stub
            Configuration conf = new Configuration();
            Job job = new Job(conf, "InverseIndex");
            job.setJarByClass(InverseIndex.class);
            job.setNumReduceTasks(4);
            job.setMapperClass(WordCount.InverseIndexMapper.class);
            job.setCombinerClass(WordCount.InverseIndexMapper.InverseIndexCombiner.class);
            job.setReducerClass(InverseIndexReducer.class);
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(Text.class);
            job.setInputFormatClass(TextInputFormat.class);
            job.setOutputFormatClass(TextOutputFormat.class);
            FileInputFormat.setInputPaths(job, new Path(args[0]));
            FileOutputFormat.setOutputPath(job, new Path(args[1]));
            job.waitForCompletion(true);
        }
    }
}
