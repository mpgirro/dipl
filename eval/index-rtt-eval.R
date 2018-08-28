
# 
# Boxplot describing the distribution of overall roundtrip times for 200 feeds with 70 items each
# Comparing both Akka and MSA in one image
#
akka_index_i70_f200_rtt_progress <- read.table("../src/benchmark/data/akka-index-i70-f200-rtt-progress.csv", header=T, sep=";");
msa_index_i70_f200_rtt_progress <- read.table("../src/benchmark/data/msa-index-i70-f200-rtt-progress.csv", header=T, sep=";");

# add a column stating where the data comes from, for later descrimination
akka_index_i70_f200_rtt_progress$origin <- rep("Akka",nrow(akka_index_i70_f200_rtt_progress));
msa_index_i70_f200_rtt_progress$origin <- rep("MSA",nrow(msa_index_i70_f200_rtt_progress));

# draw boxplot comparing the mean latency per message, that is the timespan between sending and receiving
meanMsgL_index.data <- rbind( akka_index_i70_f200_rtt_progress[,c("meanMsgL","origin")], msa_index_i70_f200_rtt_progress[,c("meanMsgL","origin")]); 
pdf("eval-index-rtt-meanMsgL.pdf");
boxplot(meanMsgL ~ origin, data=meanMsgL_index.data, outline=FALSE);  
dev.off();

# draw boxplot comparing the overall runtime, that is from the point the message was received until the answer was returned
overallRT_index.data <- rbind( akka_index_i70_f200_rtt_progress[,c("overallRT","origin")], msa_index_i70_f200_rtt_progress[,c("overallRT","origin")]); 
pdf("eval-index-rtt-overall.pdf");
boxplot(overallRT ~ origin, data=overallRT_index.data, outline=FALSE); 
dev.off();