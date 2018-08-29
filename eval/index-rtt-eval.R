# 
# Boxplot describing the distribution of overall roundtrip times for 200 feeds with 70 items each
# Comparing both Akka and MSA in one image
#
akka_index_i70_f200_rtt_progress <- read.table("../src/benchmark/data/akka-index710-rtt-progress_2018.08.29.12.16.25.csv", header=T, sep=";");
msa_index_i70_f200_rtt_progress <- read.table("../src/benchmark/data/msa-index710-rtt-progress_2018.08.29.12.06.35.csv", header=T, sep=";");

# add a column stating where the data comes from, for later descrimination
akka_index_i70_f200_rtt_progress$origin <- rep("Akka",nrow(akka_index_i70_f200_rtt_progress));
msa_index_i70_f200_rtt_progress$origin <- rep("MSA",nrow(msa_index_i70_f200_rtt_progress));

# draw boxplot comparing the mean latency per message, that is the timespan between sending and receiving
pdf("eval-index-rtt-meanMsgL.pdf");

meanMsgL_index.data <- rbind( akka_index_i70_f200_rtt_progress[,c("meanMsgL","origin")], msa_index_i70_f200_rtt_progress[,c("meanMsgL","origin")]); 
boxplot(meanMsgL ~ origin, data=meanMsgL_index.data, outline=FALSE, ylab="Mean message latency (milliseconds)");  

dev.off();

# draw boxplot comparing the overall runtime, that is from the point the message was received until the answer was returned
pdf("eval-index-rtt-overall.pdf");

overallRT_index.data <- rbind( akka_index_i70_f200_rtt_progress[,c("overallRT","origin")], msa_index_i70_f200_rtt_progress[,c("overallRT","origin")]); 
boxplot(overallRT ~ origin, data=overallRT_index.data, outline=FALSE, ylab="Processing time (milliseconds)"); 

dev.off()