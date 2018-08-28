akka_index_overall_data <- read.table("../src/benchmark/akka-index-rtt-overall.csv", header=T, sep=";");
msa_index_overall_data <- read.table("../src/benchmark/msa-index-rtt-overall.csv", header=T, sep=";");

akka_index_overall_data$color <- rep("red",nrow(akka_index_overall_data));
msa_index_overall_data$color <- rep("blue",nrow(msa_index_overall_data));


x1 <- akka_index_overall_data$input_size;
y1 <- akka_index_overall_data$overallRT;
x1.lm <- lm(y1~x1);
#plot(x1,y1, col="red");
#abline(x1.lm, col="red");

x2 <- msa_index_overall_data$input_size;
y2 <- msa_index_overall_data$overallRT;
x2.lm <- lm(y2~x2);
#plot(x2,y2, col="blue");
#abline(x2.lm, col="blue");


#plot(x1,y1, col="red", type="l");
#plot(x2,y2, col="blue", type="b");


search_overall_data <- rbind( akka_index_overall_data, msa_index_overall_data);



plot(search_overall_data$input_size,search_overall_data$overallRT, 
	col=search_overall_data$color, 
	pch = 4,
	main = "Overall Round Trip Time for Search",
	xlab = "Input Feeds",
	ylab = "Overall Processing Time [ms]");
abline(x1.lm, col="red");
abline(x2.lm, col="blue", lty=2);
legend(25, 150000, legend=c("Akka", "MSA"),
       col=c("red", "blue"), lty=1:2, cex=0.8)



pdf("eval-index-overall.pdf");

plot(x2, y2, type="l", pch=18, col="blue", lty=2, cex=1.2, xlab = "Input Feeds", ylab = "Overall Processing Time [ms]")
lines(x1, y1, pch=19, col="red", type="l")			   
legend(25, 150000, legend=c("Akka", "MSA"),
       col=c("red", "blue"), lty=1:2, cex=1.2)
	   
dev.off();	   
	   
	   
#png("index-overall-eval.png", width = 960, height = 960, units = "px", res=120, pointsize = 12);
pdf("eval-index-overall.pdf");


dev.off();

# Generate some data
#x<-1:10; y1=x*x; y2=2*y1
#plot(x, y1, type="b", pch=19, col="red", xlab="x", ylab="y")
# Add a line
#lines(x, y2, pch=18, col="blue", type="b", lty=2)
# Add a legend
#legend(1, 95, legend=c("Line 1", "Line 2"),
#       col=c("red", "blue"), lty=1:2, cex=0.8)