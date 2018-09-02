
quartzFonts(cmu_sans = c("CMU Sans Serif", "CMU Sans Serif Bold", "CMU Sans Serif Oblique", 
                         "CMU Sans Serif BoldOblique"))
#quartzFonts(cmu_serif = c("CMU Serif Roman", "CMU Serif Bold", "CMU Serif Italic", 
#                          "CMU Serif BoldItalic"))

library(extrafont)
font_import(pattern = "CM")
loadfonts()


index_akka_data <- read.table("data/index-akka-rtt-overall.csv", header=T, sep=";");
index_msa_data <- read.table("data/index-msa-rtt-overall.csv", header=T, sep=";");

# milliseconds -> seconds
index_akka_data$overallRT <- index_akka_data$overallRT / 1000
index_msa_data$overallRT <- index_msa_data$overallRT / 1000

# add color rows
index_akka_data$color <- rep("red",nrow(index_akka_data));
index_msa_data$color <- rep("blue",nrow(index_msa_data));

# aggregate the values for every X, so we'll just show the means of them
a <- aggregate(index_akka_data$overallRT, list(input_size=index_akka_data$input_size), mean)
b <- aggregate(index_msa_data$overallRT, list(input_size=index_msa_data$input_size), mean)

# this function plots the graphic of the data
g <- function() {
  plot(b$input_size, b$x, 
       type="l", pch=18, col="blue", lty=2, lwd=2,
       cex.axis = 1.3, cex.lab = 1.3,
       xlab = "Number of Feeds", ylab = "Overall Runtime [seconds]",
       #xlab="", ylab="",
       xlim=c(0,500), ylim=c(0, 400))
  #xaxt="n", yaxt="n")
  lines(a$input_size, a$x, pch=19, col="red", type="l", lwd=2)	
  legend("topleft", legend=c("Akka", "MSA"),
         col=c("red", "blue"), lty=1:2, cex=1.3, lwd=2)
}

# execute this to show in Rstudio
par(family = "cmu_sans")
g()


dest <- "out/eval-index-overall"

# output PNG
png(paste(dest, ".png", sep=""), width = 5, height = 5, units = 'in', res = 300)
par(family = "cmu_sans")
g()
dev.off()

# output PDF
pdf(paste(dest, ".pdf", sep=""), family="CM Sans", width = 5, height = 5)
par(family = "CM Sans")
g()
dev.off()
# embed the CM Sans font into the PDF, or printing might become a problem
embed_fonts(paste(dest, ".pdf", sep=""), outfile=paste(dest, "_embed.pdf", sep=""))



#
# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
#



pdf("out/eval-index-overall.pdf")
#png("out/eval-index-overall.png", width=400, height=350, res=72)

par(family = "cmu_sans")


# draw an axis on the left 
#axis(2, at=b$x,labels=b$x, col.axis="red", las=2)

# draw an axis on the bottom 
#axis(1, at=c(0,100,200,300,400,500),labels=c(0,100,200,300,400,500), cex=1.2)

#title(main="My Title", col.main="red", 
#      sub="My Sub-title", col.sub="blue", 
#      xlab="My X label", ylab="My Y label",
#      col.lab="green", cex.lab=1.5)


dev.off();


#
# Plot the data points and draw approximation line
#
x1 <- index_akka_data$input_size;
y1 <- index_akka_data$overallRT;
x1.lm <- lm(y1~x1);

x2 <- index_msa_data$input_size;
y2 <- index_msa_data$overallRT;
x2.lm <- lm(y2~x2);


search_overall_data <- rbind( index_akka_data, index_msa_data);


plot(search_overall_data$input_size,search_overall_data$overallRT, 
     col=search_overall_data$color, 
     pch = 4,
     main = "Overall Round Trip Time for Search",
     xlab = "Input Feeds", ylab = "Overall Processing Time [seconds]",
     xlim=c(0,500), ylim=c(0, 400));
abline(x1.lm, col="red");
abline(x2.lm, col="blue", lty=2);
legend("topleft", legend=c("Akka", "MSA"),
       col=c("red", "blue"), lty=1:2, cex=1)



pdf("eval-index-overall.pdf");

plot(x2, y2, 
     type="l", pch=18, col="blue", lty=2, cex=1.2, 
     xlab = "Input Feeds", ylab = "Overall Processing Time [seconds]",
     xlim=c(0,500), ylim=c(0, 400))
lines(x1, y1, pch=19, col="red", type="l")			   
legend("topleft", legend=c("Akka", "MSA"),
       col=c("red", "blue"), lty=1:2, cex=1)

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
