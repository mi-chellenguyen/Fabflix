use moviedb;

DROP PROCEDURE IF EXISTS add_movie; 
DELIMITER $$ 

CREATE PROCEDURE add_movie(IN title VARCHAR(100), 
						   IN year INTEGER,
                           IN director VARCHAR(100),
                           IN star_name VARCHAR(100),
                           IN genre_name VARCHAR(32),
                           IN rating FLOAT,
                           IN num_votes INTEGER,
                           OUT add_movie_results VARCHAR(100))
		BEGIN
			/* Query to get if a movie exists */
            SET @checkMovieExistsResults := 0;
            SELECT COUNT(*) INTO @checkMovieExistsResults
                 FROM movies M
                 WHERE M.title = title AND M.year = year AND M.director = director;
					
			/* ----------- MOVIE DOESN'T EXIST  ------------------ */
			/* If the count == 0, then the movie does not exist */
            IF @checkMovieExistsResults = 0 THEN
            
				/* Insert a new movie into movies table */
				SET @maxMovieIdResult = (SELECT MAX(id) FROM movies WHERE movies.id LIKE binary 'tt%');
                                
                SET @maxMovieNum = CAST(SUBSTRING(@maxMovieIdResult, 3) AS UNSIGNED);
				SET @incrMovieNum = @maxMovieNum + 1;
				SET @newMaxMovieId = CONCAT('tt', @incrMovieNum); 
		
                
				INSERT INTO movies (id, title, year, director) VALUES (@newMaxMovieId, title, year, director);
				INSERT INTO ratings (movieId, rating, numVotes) VALUES (@newMaxMovieId, rating, num_votes);
                
                /* ----------- CHECK IF STAR NAME EXISTS  ------------------ */
				/* Query to check if star name exists */
				SET @checkStarExistsResults = 0;
				SELECT COUNT(*) INTO @checkStarExistsResults
					 FROM stars S
					 WHERE S.name = star_name;
			                
				IF @checkStarExistsResults = 0 THEN   /* Star doesn't exist, we create a new star & link to movie */
					SET @maxStarIdResult = (SELECT MAX(id) FROM stars);
                    SET @maxStarNum = CAST(SUBSTRING(@maxStarIdResult, 3) AS UNSIGNED);
					SET @incrStarNum = @maxStarNum + 1;
					SET @newMaxStarId = CONCAT('nm', @incrStarNum);
                    
					INSERT INTO stars (id, name, birthYear) VALUES (@newMaxStarId, star_name, NULL);
					INSERT INTO stars_in_movies (starId, movieId) VALUES (@newMaxStarId, @newMaxMovieId);
					
                    
				ELSE	/* Star exists, link star to movie */
					SET @getStarResults = NULL;
					SELECT S.id INTO @getStarResults
						 FROM stars S
						 WHERE S.name = star_name
                         LIMIT 1;	 /* If there are multiple stars with same name, we can link any so take the first*/
					 
                    #SELECT @getStarResults;
                    
                    INSERT INTO stars_in_movies (starId, movieId) VALUES (@getStarResults, @newMaxMovieId);
                    
                    
				END IF;
                
                 /* ----------- CHECK IF GENRE NAME EXISTS  ------------------ */
                /* Query to check if genre name exists */
				SET @checkGenreExistsResults = 0;
				SELECT COUNT(*) INTO @checkGenreExistsResults
					 FROM genres G
					 WHERE G.name = genre_name;
							
                IF @checkGenreExistsResults = 0 THEN   /* Genre doesn't exist, we create a new genre and link to movie*/
					SET @maxGenreIdResult = (SELECT MAX(id) FROM genres);
                    SET @maxGenreId = CAST(@maxGenreIdResult AS UNSIGNED);
					SET @newGenreId = @maxGenreId + 1;
					
					INSERT INTO genres (id, name) VALUES (@newGenreId, genre_name);
					INSERT INTO genres_in_movies (genreId, movieId) VALUES (@newGenreId, @newMaxMovieId);
                    
                    
				ELSE	/* Genre exists, link genre to movie */
					SET @getGenreResults = NULL;
					SELECT G.id INTO @getGenreResults
						 FROM genres G
						 WHERE G.name = genre_name;
			
					#SELECT @getGenreResults;
                    
                    INSERT INTO genres_in_movies (genreId, movieId) VALUES (@getGenreResults, @newMaxMovieId);	
                 
                END IF;
				SELECT 'Added movie successfully' INTO add_movie_results; 

                                
			/* ----------- MOVIE DOES EXIST  ------------------ */
			/* If the count != 0, then the movie does exists and displays a message to the user */   
            ELSE
				SELECT 'Movie already exists' INTO add_movie_results;
            
            END IF;
        END
$$

DELIMITER ;


# Movie exists: Movie, star, and genre exist
#CALL add_movie('Chief Zabu', 2016, 'Neil Cohen', 'Susan Aufhauser', 'Comedy', @add_movie_results);
#SELECT @add_movie_results;


/* Movie doesn't exist */

# Movie, star, and genre don't exist
#CALL add_movie('Yarn', 2019, 'Neil Cohen', 'Chen Li', 'TestGenre3', @add_movie_results);
#SELECT @add_movie_results;

# Movie doesn't exist, but star and genre exist
#CALL add_movie('Green Grass', 2017, 'Neil Cohen', 'Charlton Heston', 'Documentary', @add_movie_results);
#SELECT @add_movie_results;

# Movie and star don't exist, genre exists
#CALL add_movie('BBB', 2017, 'Neil Cohen', 'Klefstad', 'Adventure', @add_movie_results);
#SELECT @add_movie_results;

# Movie and genre don't exist, star exists
#CALL add_movie('AAA', 2017, 'Neil Cohen', 'Charlton Heston', 'TestGenre4', @add_movie_results);
#SELECT @add_movie_results;

#CALL add_movie('AABBCCDD', 2019, 'Apple Bottom', 'Jared Jared', 'GenreTesting', @add_movie_results);
#SELECT @add_movie_results;

# FOR CHECKING CALL PROC RESULTS
/*
use moviedb;

SELECT M.id as movieId, M.title, M.year, M.director, G.id as genreId, G.name as genreName, S.id as starId, S.name as starName, R.rating
FROM movies M, genres G, stars S, stars_in_movies SIM, genres_in_movies GIM, ratings R
WHERE M.id = SIM.movieId AND S.id = SIM.starId AND M.id = GIM.movieId AND R.movieId = M.id
AND G.id = GIM. genreId AND GIM.movieId = SIM.movieId
AND M.title = ?;
*/

