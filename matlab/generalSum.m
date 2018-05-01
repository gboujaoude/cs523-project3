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
    virus_file_counter = 0;
    for run=1:length(subDirs)
        subStr = subDirs{run};
        cd(subStr);
        
        virus_over_time = csvread('virus-over-time.csv');
        virus_over_time = virus_over_time';
        virus_over_time = virus_over_time(1,virusMin:virusMax);
        
        virus_avg = virus_avg + virus_over_time;
        virus_file_counter = virus_file_counter + 1;
        
        cd ../
    end
    cd ../
    
    % Make figure
    virus_avg = virus_avg./virus_file_counter;
    my_png = figure;
    plot(floor(linspace(virusMin,virusMax,(virusMax-virusMin)+1)),virus_avg);
    hold on;
    xlim([virusMin virusMax]);
    ylim([0 6000]);
    newTitle = strcat('Average Virus-Over-Time:',str);
    title(newTitle,'Interpreter','none');
    xlabel('Timestep','FontSize',12);
    ylabel('Viruses','FontSize',12);
    hold off;
    imageFilename = strcat('virus-over-time-',str);
    imageDir = strcat('../matlab/plots/',imageFilename);
    saveas(my_png,imageDir,'png');
end

cd ../matlab/