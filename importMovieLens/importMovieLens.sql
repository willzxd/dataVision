create table movies_complete (
  movie_id integer not null,
  movie_title varchar(255),
  release_date date,
  video_release date,
  IMDb_URL text,
  genres_unknown integer,
  genres_Action integer,
  genres_Adventure integer,
  genres_Animation integer,
  genres_Children integer,
  genres_Comedy integer,
  genres_Crime integer,
  genres_Documentary integer,
  genres_Drama integer,
  genres_Fantasy integer,
  genres_FilmNoir integer,
  genres_Horror integer,
  genres_Musical integer,
  genres_Mystery integer,
  genres_Romance integer,
  genres_SciFi integer,
  genres_Thriller integer,
  genres_War integer,
  genres_Western integer
);

COPY movies_complete from '/Users/will/Downloads/ml-100k/uitem.csv' with csv header;

select movies_complete.*,
ratings.id as ratings_id, ratings.rating, ratings.rated_at, ratings.user_id,
users.age, users.gender, users.occupation_id, users.zip_code,
occupations.name as occupation_name
into big_rating_table
from movies_complete, ratings, users, occupations
where   movies_complete.movie_id = ratings.movie_id
        and users.id = ratings.user_id
        and users.occupation_id = occupations.id;
