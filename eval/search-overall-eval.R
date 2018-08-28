akka_search_overall_data <- read.table("../src/benchmark/akka-search-rtt-overall.csv", header=T, sep=";");
msa_search_overall_data <- read.table("../src/benchmark/msa-search-rtt-overall.csv", header=T, sep=";");

akka_search_overall_data$color <- rep("red",nrow(akka_search_overall_data));
msa_search_overall_data$color <- rep("blue",nrow(msa_search_overall_data));


x1 <- akka_search_overall_data$input_size;
y1 <- akka_search_overall_data$overallRT;
x1.lm <- lm(y1~x1);
#plot(x1,y1, col="red");
#abline(x1.lm, col="red");

x2 <- msa_search_overall_data$input_size;
y2 <- msa_search_overall_data$overallRT;
x2.lm <- lm(y2~x2);
#plot(x2,y2, col="blue");
#abline(x2.lm, col="blue");


#plot(x1,y1, col="red", type="l");
#plot(x2,y2, col="blue", type="b");


search_overall_data <- rbind( akka_search_overall_data, msa_search_overall_data);
plot(search_overall_data$input_size,search_overall_data$overallRT, col=search_overall_data$color, pch = 4);
abline(x1.lm, col="red");
abline(x2.lm, col="blue");


# only Akka
plot(x1, y1, type="b", pch=18, col="red", lty=2, cex=1.2, xlab = "Search requests", ylab = "Response Time [ms]")
abline(x1.lm, col="red");



# only MSA
plot(x2, y2, type="b", pch=19, col="blue", lty=2, cex=1.2, xlab = "Search requests", ylab = "Response Time [ms]")
abline(x2.lm, col="blue");



pdf("eval-search-overall.pdf");

plot(x2, y2, type="l", pch=18, col="blue", lty=2, cex=1.2, xlab = "Search requests", ylab = "Response Time [ms]")
lines(x1, y1, pch=19, col="red", type="l")			   
legend(1000, 200000, legend=c("Akka", "MSA"),
       col=c("red", "blue"), lty=1:2, cex=1.2)

dev.off();	   