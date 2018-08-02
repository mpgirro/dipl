#
# Round Trip Time (RTT)
#

akka_index_rtt_data <- read.table("akka-index100-rtt.csv", header=T, sep=";");
akka_search_rtt_data <- read.table("akka-search100-rtt.csv", header=T, sep=";");
msa_index_rtt_data <- read.table("msa-index100-rtt.csv", header=T, sep=";");
msa_search_rtt_data <- read.table("msa-search100-rtt.csv", header=T, sep=";");

akka_index_rtt_data$origin <- rep("Akka",nrow(akka_index_rtt_data));
akka_search_rtt_data$origin <- rep("Akka",nrow(akka_search_rtt_data));
msa_index_rtt_data$origin <- rep("MSA",nrow(msa_index_rtt_data));
msa_search_rtt_data$origin <- rep("MSA",nrow(msa_search_rtt_data));

# das hier ist ziemlich sinnlos?
meanMsgL.data <- rbind( akka_index_rtt_data[,c("meanMsgL","origin")], akka_search_rtt_data[,c("meanMsgL","origin")], msa_index_rtt_data[,c("meanMsgL","origin")], msa_search_rtt_data[,c("meanMsgL","origin")]); 
boxplot(meanMsgL ~ origin, data=meanMsgL.data);   

mml_index.data <- rbind( akka_index_rtt_data[,c("meanMsgL","origin")], msa_index_rtt_data[,c("meanMsgL","origin")]);
boxplot(meanMsgL ~ origin, 
	data=mml_index.data,
	main = "Index Subsystem - Mean Message Latency",
	xlab = "Architecture",
	ylab = "Mean Message Latency (ms)");  

mml_search.data <- rbind( akka_search_rtt_data[,c("meanMsgL","origin")], msa_search_rtt_data[,c("meanMsgL","origin")]); 
boxplot(meanMsgL ~ origin, 
	data=mml_search.data,
	main = "Search Subsystem - Mean Message Latency",
	xlab = "Architecture",
	ylab = "Mean Message Latency (ms)"); 

# -----

ort_index.data <- rbind( akka_index_rtt_data[,c("overallRT","origin")], msa_index_rtt_data[,c("overallRT","origin")]);
boxplot(overallRT ~ origin, data=ort_index.data);  

ort_search.data <- rbind( akka_search_rtt_data[,c("overallRT","origin")], msa_search_rtt_data[,c("overallRT","origin")]);
boxplot(overallRT ~ origin, data=ort_search.data);

# -----

meanRTpI_index.data <- rbind( akka_index_rtt_data[,c("meanRTpI","origin")], msa_index_rtt_data[,c("meanRTpI","origin")]);
boxplot(meanRTpI ~ origin, data=meanRTpI_index.data);

#
# Messages Per Second (MPS)
#

akka_index_mps_data <- read.table("akka-index100-mps.csv", header=T, sep=";");
akka_search_mps_data <- read.table("akka-search100-mps.csv", header=T, sep=";");
msa_index_mps_data <- read.table("msa-index100-mps.csv", header=T, sep=";");
msa_search_mps_data <- read.table("msa-search100-mps.csv", header=T, sep=";");

barplot(akka_index_mps_data$mps,
	main = "Akka Index Subsystem - Messages per Second",
	xlab = "Task Unit",
	ylab = "Messages per Second (MpS)",
	names.arg = akka_index_mps_data$task_id,
	col = "darkred");
	
barplot(akka_search_mps_data$mps,
	main = "Akka Search Subsystem - Messages per Second",
	xlab = "Task Unit",
	ylab = "Messages per Second (MpS)",
	names.arg = akka_search_mps_data$task_id,
	col = "darkred");
	
barplot(msa_index_mps_data$mps,
	main = "MSA Index Subsystem - Messages per Second",
	xlab = "Task Unit",
	ylab = "Messages per Second (MpS)",
	names.arg = msa_index_mps_data$task_id,
	col = "darkred");

barplot(msa_search_mps_data$mps,
	main = "MSA Search Subsystem - Messages per Second",
	xlab = "Task Unit",
	ylab = "Messages per Second (MpS)",
	names.arg = msa_search_mps_data$task_id,
	col = "darkred");
	
	
hist(msa_index_mps_data$mps);
hist(msa_search_mps_data$mps);

#
# Mean search response latency
#
akka_search_mean_RT_nofuture_data <- read.table("akka-search-mean-RT-nofuture.csv", header=T, sep=";");
#akka_mean_rt <- subset(akka_search_mean_RT_nofuture_data, select=c("queries_size", "mean_response_time"));

# approximation line
x <- akka_search_mean_RT_nofuture_data$queries_size
y <- akka_search_mean_RT_nofuture_data$mean_response_time
x.lm <- lm(y~x)
plot(x,y)
abline(x.lm, col="red")

#
#
akka_search_mean_RT_withfuture_data <- read.table("akka-search-mean-RT-withfuture.csv", header=T, sep=";");

# mean response time
x <- akka_search_mean_RT_withfuture_data$queries_size
y <- akka_search_mean_RT_withfuture_data$mean_response_time
x.lm <- lm(y~x)
plot(x,y)
abline(x.lm, col="red")

# overall RT
x <- akka_search_mean_RT_withfuture_data$queries_size
y <- akka_search_mean_RT_withfuture_data$overall_rt
x.lm <- lm(y~x)
plot(x,y)
abline(x.lm, col="red")

#
#
msa_search_mean_RT_data <- read.table("msa-search-mean-RT.csv", header=T, sep=";");
#plot(subset(msa_search_mean_RT_data, select=c("queries_size", "mean_response_time")));
x <- msa_search_mean_RT_data$queries_size
y <- msa_search_mean_RT_data$mean_response_time
x.lm <- lm(y~x)
plot(x,y)
abline(x.lm, col="red")



akka_search_overall_data <- read.table("../src/benchmark/akka-search-rtt-overall.csv", header=T, sep=";");
msa_search_overall_data <- read.table("../src/benchmark/msa-search-rtt-overall.csv", header=T, sep=";");

akka_search_overall_data$color <- rep("red",nrow(akka_search_overall_data));
#akka_search_overall_data$input_size.lm <- lm(akka_search_overall_data$mean_rtt_per_item ~ akka_search_overall_data$input_size)

msa_search_overall_data$color <- rep("blue",nrow(msa_search_overall_data));
#msa_search_overall_data$input_size.lm <- lm(msa_search_overall_data$mean_rtt_per_item ~ msa_search_overall_data$input_size)

x1 <- akka_search_overall_data$input_size
y1 <- akka_search_overall_data$mean_rtt_per_item
x1.lm <- lm(y1~x1)
plot(x1,y1, col="red")
abline(x1.lm, col="red")

x2 <- msa_search_overall_data$input_size
y2 <- msa_search_overall_data$mean_rtt_per_item
x2.lm <- lm(y2~x2)
plot(x2,y2, col="blue")
abline(x2.lm, col="blue")

plot(x1,y1, col="red", type="l")
plot(x2,y2, col="blue", type="b")


search_overall_data <- rbind( akka_search_overall_data, msa_search_overall_data);
plot(search_overall_data$input_size,search_overall_data$mean_rtt_per_item, col=search_overall_data$color, pch = 4) # pch = 4 --> X symbols, seehttp://www.sthda.com/english/wiki/r-plot-pch-symbols-the-different-point-shapes-available-in-r
abline(x1.lm, col="red")
abline(x2.lm, col="blue")



akka_search100_rtt_progress <- read.table("../src/benchmark/data/akka-search100-rtt-progress_2018.07.29.00.42.32.csv", header=T, sep=";");
boxplot(akka_search100_rtt_progress$meanRTpI);
akka_search500_rtt_progress <- read.table("../src/benchmark/data/akka-search500-rtt-progress_2018.07.28.12.40.47.csv", header=T, sep=";");
boxplot(akka_search500_rtt_progress$meanRTpI);
akka_search1500_rtt_progress <- read.table("../src/benchmark/data/akka-search1500-rtt-progress_2018.07.28.12.44.44.csv", header=T, sep=";");
boxplot(akka_search1500_rtt_progress$meanRTpI);
akka_search5000_rtt_progress <- read.table("../src/benchmark/data/akka-search5000-rtt-progress_2018.07.28.12.49.41.csv", header=T, sep=";");
boxplot(akka_search5000_rtt_progress$meanRTpI, outline=FALSE);
akka_search10000_rtt_progress <- read.table("../src/benchmark/data/akka-search10000-rtt-progress_2018.08.01.16.47.00.csv", header=T, sep=";");
boxplot(akka_search10000_rtt_progress$meanRTpI, outline=FALSE);

hist(akka_search10000_rtt_progress$meanRTpI, col="red")


akka_index2100_rtt_progress <- read.table("../src/benchmark/data/akka-index2100-rtt-progress_2018.07.29.12.33.11.csv", header=T, sep=";");
boxplot(akka_index2100_rtt_progress$meanRTpI);



# 
# Boxplot describing the distribution of overall roundtrip times for 10k retrievals
# Comparing both Akka and MSA in one image
#
akka_search10000_rtt_progress <- read.table("../src/benchmark/data/akka-search10000-rtt-progress_2018.08.01.16.47.00.csv", header=T, sep=";");
msa_search10000_rtt_progress <- read.table("../src/benchmark/data/msa-search10000-rtt-progress_2018.08.01.15.58.09.csv", header=T, sep=";");

# add a column stating where the data comes from, for later descrimination
akka_search10000_rtt_progress$origin <- rep("Akka",nrow(akka_search10000_rtt_progress));
msa_search10000_rtt_progress$origin <- rep("MSA",nrow(msa_search10000_rtt_progress));

# draw boxplot comparing the mean latency per message, that is the timespan between sending and receiving
meanMsgL_search.data <- rbind( akka_search10000_rtt_progress[,c("meanMsgL","origin")], msa_search10000_rtt_progress[,c("meanMsgL","origin")]); 
boxplot(meanMsgL ~ origin, data=meanMsgL_search.data, outline=FALSE);  

# draw boxplot comparing the overall runtime, that is from the point the message was received until the answer was returned
overallRT_search.data <- rbind( akka_search10000_rtt_progress[,c("overallRT","origin")], msa_search10000_rtt_progress[,c("overallRT","origin")]); 
boxplot(overallRT ~ origin, data=overallRT_search.data, outline=FALSE);  

# - - - - - - - - - - - - - - - - - - -

# select all rows where the overall RT is > 13ms (which is the threshold for the upper wisker of the MSA boxblot -> everything above is an outlier)
overallRT_search.outliers <- overallRT_search.data[(overallRT_search.data[,1]>13),]

overallRT_search.outliers_akka <- overallRT_search.outliers[(overallRT_search.outliers[,2]=="Akka"),]
overallRT_search.outliers_msa <- overallRT_search.outliers[(overallRT_search.outliers[,2]=="MSA"),]

hist(overallRT_search.outliers_akka$overallRT)
hist(overallRT_search.outliers_msa$overallRT)