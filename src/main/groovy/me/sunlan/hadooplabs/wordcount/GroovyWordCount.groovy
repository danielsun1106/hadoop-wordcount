/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package me.sunlan.hadooplabs.wordcount

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.IntWritable
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.Job
import org.apache.hadoop.mapreduce.Mapper
import org.apache.hadoop.mapreduce.Reducer
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat

class GroovyWordCount {

    static class TokenizerMapper
            extends Mapper<Object, Text, Text, IntWritable> {

        private final static IntWritable one = new IntWritable(1)
        private Text word = new Text()

        @Override
        void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            StringTokenizer itr = new StringTokenizer(value.toString())
            while (itr.hasMoreTokens()) {
                word.set(itr.nextToken())
                context.write(word, one)
            }
        }
    }

    static class IntSumReducer
            extends Reducer<Text, IntWritable, Text, IntWritable> {
        private IntWritable result = new IntWritable()

        @Override
        void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
            int sum = values.collect(e -> e.get()).sum()
            result.set(sum)
            context.write(key, result)
        }
    }

    static void main(String[] args) throws Exception {
        Configuration conf = new Configuration()
        Job job = Job.getInstance(conf, "word count")
        job.with {
        	jarByClass = GroovyWordCount
        	mapperClass = TokenizerMapper
        	combinerClass = IntSumReducer
        	reducerClass = IntSumReducer
        	outputKeyClass = Text
        	outputValueClass = IntWritable
        }
        FileInputFormat.addInputPath(job, new Path(args[0]))
        FileOutputFormat.setOutputPath(job, new Path(args[1]))
        System.exit(job.waitForCompletion(true) ? 0 : 1)
    }
}
