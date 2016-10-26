cd('C:\Users\Owner\Documents\Portugal\Error_Space_GP\ESGSGP\GSGP\results');
#fprintf('%f',pwd);
population = dlmread('population.txt',";");
#outputs = csvread('mindividuals/outputs.txt')
#fprintf('%f',size(population));
fprintf('%f',population(1,:));
