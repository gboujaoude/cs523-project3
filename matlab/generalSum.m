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
    
    virus_avg = virus_avg./virus_file_counter;
    figure;
    plot(floor(linspace(virusMin,virusMax,(virusMax-virusMin)+1)),virus_avg);
    hold on;
    xlim([virusMin virusMax]);
    title(str);
    hold off;
    cd ../
end

cd ../matlab/