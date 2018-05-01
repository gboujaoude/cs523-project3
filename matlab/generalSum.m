%temp =
%csvread('../data/control_group/2018-04-30T12-54-31/virus-over-time.csv');

% SETUP
virusMin = 50;
virusMax = 500;

% Get config group names
cd ../data/
files = dir;
configGroups = {files([files.isdir]).name};
configGroups = configGroups(~ismember(configGroups,{'.','..'}));

% Enter each group. Take the average.
for group=1:length(configGroups)
    str = configGroups{group};
    cd(str);
    
    files = dir;
    subDirs = {files([files.isdir]).name};
    subDirs = subDirs(~ismember(subDirs,{'.','..'}));
    
    % Iterate virus-over-time file in each subdirectory
    virus_avg = zeros(1,(virusMax-virusMin)+1);
    health_avg = zeros(1,(virusMax-virusMin)+1);
    infected_avg = zeros(1,(virusMax-virusMin)+1);
    lympho_avg = zeros(1,(virusMax-virusMin)+1);
    file_counter = 0;
    for run=1:length(subDirs)
        subStr = subDirs{run};
        cd(subStr);
        
        virus_over_time = csvread('virus-over-time.csv');
        virus_over_time = virus_over_time';
        virus_over_time = virus_over_time(1,virusMin:virusMax);
        
        health_over_time = csvread('healthy-over-time.csv');
        health_over_time = health_over_time';
        health_over_time = health_over_time(1,virusMin:virusMax);
        
        infected_over_time = csvread('infected-over-time.csv');
        infected_over_time = infected_over_time';
        infected_over_time = infected_over_time(1,virusMin:virusMax);
        
        lympho_over_time = csvread('lymphocytes-over-time.csv');
        lympho_over_time = lympho_over_time';
        lympho_over_time = lympho_over_time(1,virusMin:virusMax);
        
        virus_avg = virus_avg + virus_over_time;
        health_avg = health_avg + health_over_time;
        infected_avg = infected_avg + infected_over_time;
        lympho_avg = lympho_avg + lympho_over_time;
        
        file_counter = file_counter + 1;
        cd ../
    end
    cd ../
    
    % Make figure
    virus_avg = virus_avg./file_counter;
    health_avg = health_avg./file_counter;
    infected_avg = infected_avg./file_counter;
    lympho_avg = lympho_avg./file_counter;
    
    my_png = figure;
    plot(floor(linspace(virusMin,virusMax,(virusMax-virusMin)+1)),virus_avg,'-r');
    hold on;
    plot(floor(linspace(virusMin,virusMax,(virusMax-virusMin)+1)),health_avg,'-b');
    plot(floor(linspace(virusMin,virusMax,(virusMax-virusMin)+1)),infected_avg,'-k');
    plot(floor(linspace(virusMin,virusMax,(virusMax-virusMin)+1)),lympho_avg,'-g');
        
    xlim([virusMin virusMax]);
    ylim([0 3000]);
    
    newTitle = strcat('Immune response over time:',str);
    title(newTitle,'Interpreter','none');
    xlabel('Timestep','FontSize',12);
    ylabel('Amount','FontSize',12);
    legend('viruses','healthy cells','infected cells','lymphocytes');
    
    hold off;
    
    imageFilename = strcat(str);
    imageDir = strcat('../matlab/plots/',imageFilename);
    saveas(my_png,imageDir,'png');
end

cd ../matlab/